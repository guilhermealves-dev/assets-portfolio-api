package dev.guilhermealves.assets.portfolio.api.cucumber;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.AssetRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.TransactionRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.UserRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.WalletRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.HoldingResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.PortfolioSummaryResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.WalletSummaryResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.TransactionType;
import io.cucumber.java.After;
import io.cucumber.java.ParameterType;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class PortfolioAnalyticsStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userId;
    private String walletId;
    private final Map<String, String> assetIdsBySymbol = new HashMap<>();
    private final List<String> createdTxIds = new ArrayList<>();
    private int txMinuteCounter = 0;

    private List<HoldingResponse> lastHoldings;
    private WalletSummaryResponse lastWalletSummary;
    private PortfolioSummaryResponse lastPortfolioSummary;
    private List<HoldingResponse> lastPortfolioHoldings;

    private final String runId = UUID.randomUUID().toString().substring(0, 8);

    private String getAuthHeader() {
        return "Bearer test-token-" + (userId != null ? userId : "test-user");
    }

    @Dado("que eu crio um usuário para testes analíticos")
    public void criarUsuarioAnalitico() throws Exception {
        txMinuteCounter = 0;
        UserRequest request = new UserRequest();
        request.setName("Investidor Analítico " + runId);
        request.setEmail("analytics." + runId + "@portfolio.dev");
        request.setPassword("senhaSegura123");

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        userId = node.get("id").asText();
        assertNotNull(userId);
    }

    @Dado("que eu crio uma carteira de investimentos analítica")
    public void criarCarteiraAnalitica() throws Exception {
        WalletRequest request = new WalletRequest();
        request.setName("Carteira Analítica " + runId);
        request.setUserId(userId);
        request.setIcon("📊");
        request.setColor("#4E73DF");

        MvcResult result = mockMvc.perform(post("/wallets")
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        walletId = node.get("id").asText();
        assertNotNull(walletId);
    }

    @E("eu cadastro um ativo com símbolo {string} e nome {string}")
    public void cadastrarAtivo(String simbolo, String nome) throws Exception {
        AssetRequest request = new AssetRequest();
        request.setSymbol(simbolo);
        request.setName(nome);
        request.setType(AssetType.CRYPTO);
        request.setImgUrl("https://s2.coinmarketcap.com/static/img/coins/32x32/1.png");

        MvcResult result = mockMvc.perform(post("/assets")
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        String id = node.get("id").asText();
        assetIdsBySymbol.put(simbolo, id);
    }

    @ParameterType("-?[0-9]+(?:[\\.,][0-9]+)?")
    public BigDecimal decimal(String value) {
        return new BigDecimal(value.replace(',', '.'));
    }

    @Quando("eu registro uma compra de {decimal} cotas do ativo {string} ao preço unitário de {decimal}")
    public void registrarCompra(BigDecimal quantidade, String simbolo, BigDecimal preco) throws Exception {
        registrarTransacao(TransactionType.BUY, quantidade, simbolo, preco);
    }

    @E("eu registro uma venda de {decimal} cotas do ativo {string} ao preço unitário de {decimal}")
    public void registrarVenda(BigDecimal quantidade, String simbolo, BigDecimal preco) throws Exception {
        registrarTransacao(TransactionType.SELL, quantidade, simbolo, preco);
    }

    private void registrarTransacao(TransactionType tipo, BigDecimal quantidade, String simbolo, BigDecimal preco) throws Exception {
        String assetId = assetIdsBySymbol.get(simbolo);
        assertNotNull(assetId, "Ativo não encontrado: " + simbolo);

        BigDecimal montante = preco.multiply(quantidade);

        TransactionRequest request = new TransactionRequest();
        request.setType(tipo);
        request.setWalletId(walletId);
        request.setAssetId(assetId);
        request.setPrice(preco);
        request.setQuantity(quantidade);
        request.setAmount(montante);
        request.setFee(BigDecimal.ZERO);
        request.setDatetime(String.format("2026-11-12T10:%02d:00", ++txMinuteCounter));

        MvcResult result = mockMvc.perform(post("/transactions")
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        createdTxIds.add(node.get("id").asText());
    }

    @Quando("eu consulto os holdings da carteira")
    public void consultarHoldingsCarteira() throws Exception {
        MvcResult result = mockMvc.perform(get("/wallets/{id}/holdings", walletId)
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andReturn();

        lastHoldings = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<HoldingResponse>>() {});
    }

    @Então("a posição do ativo {string} deve ter quantidade {decimal} e preço médio {decimal}")
    public void validarPosicao(String simbolo, BigDecimal quantEsperada, BigDecimal precoMedioEsperado) {
        HoldingResponse holding = findHoldingBySymbol(lastHoldings, simbolo);
        assertNotNull(holding, "Holding não encontrado para o símbolo: " + simbolo);

        assertEquals(0, quantEsperada.compareTo(holding.getQuantity()),
                "Quantidade divergente para " + simbolo + " (esperado: " + quantEsperada + ", obtido: " + holding.getQuantity() + ")");
        assertEquals(0, precoMedioEsperado.compareTo(holding.getAvgBuyPrice()),
                "Preço médio divergente para " + simbolo + " (esperado: " + precoMedioEsperado + ", obtido: " + holding.getAvgBuyPrice() + ")");
    }

    @E("a posição do ativo {string} deve ter lucro realizado de {decimal} e lucro não realizado de {decimal}")
    public void validarLucrosPosicao(String simbolo, BigDecimal realizadoEsperado, BigDecimal naoRealizadoEsperado) {
        HoldingResponse holding = findHoldingBySymbol(lastHoldings, simbolo);
        assertNotNull(holding, "Holding não encontrado para o símbolo: " + simbolo);

        assertEquals(0, realizadoEsperado.compareTo(holding.getRealizedProfitLoss()),
                "Lucro realizado divergente para " + simbolo + " (esperado: " + realizadoEsperado + ", obtido: " + holding.getRealizedProfitLoss() + ")");
        assertEquals(0, naoRealizadoEsperado.compareTo(holding.getUnrealizedProfitLoss()),
                "Lucro não realizado divergente para " + simbolo + " (esperado: " + naoRealizadoEsperado + ", obtido: " + holding.getUnrealizedProfitLoss() + ")");
    }

    @Quando("eu consulto o resumo da carteira")
    public void consultarResumoCarteira() throws Exception {
        MvcResult result = mockMvc.perform(get("/wallets/{id}/summary", walletId)
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andReturn();

        lastWalletSummary = objectMapper.readValue(result.getResponse().getContentAsString(), WalletSummaryResponse.class);
    }

    @Então("o resumo da carteira deve apresentar saldo total de {decimal} e total investido de {decimal}")
    public void validarResumoCarteiraTotais(BigDecimal saldoEsperado, BigDecimal investidoEsperado) {
        assertNotNull(lastWalletSummary);
        assertEquals(0, saldoEsperado.compareTo(lastWalletSummary.getTotalBalance()),
                "Saldo total divergente (esperado: " + saldoEsperado + ", obtido: " + lastWalletSummary.getTotalBalance() + ")");
        assertEquals(0, investidoEsperado.compareTo(lastWalletSummary.getTotalInvested()),
                "Total investido divergente (esperado: " + investidoEsperado + ", obtido: " + lastWalletSummary.getTotalInvested() + ")");
    }

    @E("o lucro não realizado da carteira deve ser {decimal} e o lucro realizado deve ser {decimal}")
    public void validarResumoCarteiraLucros(BigDecimal naoRealizadoEsperado, BigDecimal realizadoEsperado) {
        assertNotNull(lastWalletSummary);
        assertEquals(0, naoRealizadoEsperado.compareTo(lastWalletSummary.getUnrealizedProfitLoss()),
                "Lucro não realizado divergente (esperado: " + naoRealizadoEsperado + ", obtido: " + lastWalletSummary.getUnrealizedProfitLoss() + ")");
        assertEquals(0, realizadoEsperado.compareTo(lastWalletSummary.getRealizedProfitLoss()),
                "Lucro realizado divergente (esperado: " + realizadoEsperado + ", obtido: " + lastWalletSummary.getRealizedProfitLoss() + ")");
    }

    @E("o lucro histórico total \\(allTimeProfitLoss) da carteira deve ser {decimal}")
    public void validarLucroHistoricoTotal(BigDecimal lucroTotalEsperado) {
        assertNotNull(lastWalletSummary);
        assertEquals(0, lucroTotalEsperado.compareTo(lastWalletSummary.getAllTimeProfitLoss()));
    }

    @Quando("eu consulto o resumo global do portfólio")
    public void consultarResumoGlobal() throws Exception {
        MvcResult result = mockMvc.perform(get("/portfolio/summary")
                        .header("Authorization", getAuthHeader())
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andReturn();

        lastPortfolioSummary = objectMapper.readValue(result.getResponse().getContentAsString(), PortfolioSummaryResponse.class);
    }

    @Então("o portfólio deve apresentar saldo total de {decimal} e lucro total de {decimal}")
    public void validarResumoGlobal(BigDecimal saldoEsperado, BigDecimal lucroEsperado) {
        assertNotNull(lastPortfolioSummary);
        assertEquals(0, saldoEsperado.compareTo(lastPortfolioSummary.getTotalBalance()));
        assertEquals(0, lucroEsperado.compareTo(lastPortfolioSummary.getAllTimeProfitLoss()));
    }

    @Quando("eu consulto os holdings globais do portfólio")
    public void consultarHoldingsGlobais() throws Exception {
        MvcResult result = mockMvc.perform(get("/portfolio/holdings")
                        .header("Authorization", getAuthHeader())
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andReturn();

        lastPortfolioHoldings = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<HoldingResponse>>() {});
    }

    @Então("a lista global de holdings deve conter {int} ativos sob custódia")
    public void validarQuantidadeHoldingsGlobais(Integer qtd) {
        assertNotNull(lastPortfolioHoldings);
        long ativosSobCustodia = lastPortfolioHoldings.stream()
                .filter(h -> h.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .count();
        assertEquals(qtd.longValue(), ativosSobCustodia);
    }

    @Quando("eu finalizo e deleto todos os dados de teste analíticos")
    public void finalizarEDeletarDados() throws Exception {
        cleanup();
    }

    @Então("todos os dados analíticos devem ter sido removidos com sucesso")
    public void validarRemocaoDados() throws Exception {
        if (walletId != null) {
            mockMvc.perform(get("/wallets/{id}", walletId)
                            .header("Authorization", getAuthHeader()))
                    .andExpect(status().isNotFound());
        }
    }

    @After
    public void cleanup() {
        try {
            for (String txId : createdTxIds) {
                mockMvc.perform(delete("/transactions/{id}", txId)
                        .header("Authorization", getAuthHeader()));
            }
            createdTxIds.clear();

            if (walletId != null) {
                mockMvc.perform(delete("/wallets/{id}", walletId)
                        .header("Authorization", getAuthHeader()));
                walletId = null;
            }

            for (String assetId : assetIdsBySymbol.values()) {
                mockMvc.perform(delete("/assets/{id}", assetId)
                        .header("Authorization", getAuthHeader()));
            }
            assetIdsBySymbol.clear();

            if (userId != null) {
                mockMvc.perform(delete("/users/{id}", userId)
                        .header("Authorization", getAuthHeader()));
                userId = null;
            }
        } catch (Exception e) {
            log.warn("Erro no cleanup analítico: {}", e.getMessage());
        }
    }

    private HoldingResponse findHoldingBySymbol(List<HoldingResponse> list, String symbol) {
        if (list == null) return null;
        return list.stream()
                .filter(h -> h.getAsset() != null && symbol.equalsIgnoreCase(h.getAsset().getSymbol()))
                .findFirst()
                .orElse(null);
    }
}
