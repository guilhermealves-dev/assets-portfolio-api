package dev.guilhermealves.assets.portfolio.api.cucumber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.TransactionRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.UserRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.WalletRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.PortfolioChartResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.WalletSummaryResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.TransactionType;
import io.cucumber.java.After;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class Fase5StepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userId;
    private String walletId;
    private final List<String> createdTxIds = new ArrayList<>();
    private final Set<String> createdAssetIds = new HashSet<>();
    private MvcResult lastResult;
    private WalletSummaryResponse lastWalletSummary;
    private PortfolioChartResponse lastChartResponse;
    private int txCounter = 0;

    private final String runId = UUID.randomUUID().toString().substring(0, 8);

    private String getAuthHeader() {
        return "Bearer test-token-" + (userId != null ? userId : "test-user");
    }

    @Dado("que eu crio um usuário autenticado para testes da Fase 5")
    public void criarUsuarioFase5() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("Investidor Fase5 " + runId);
        request.setEmail("fase5." + runId + "@portfolio.dev");

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        userId = node.get("id").asText();
        assertNotNull(userId);
    }

    @E("eu crio uma carteira de investimentos para a Fase 5")
    public void criarCarteiraFase5() throws Exception {
        WalletRequest request = new WalletRequest();
        request.setName("Carteira Fase5 " + runId);
        request.setUserId(userId);
        request.setIcon("📈");
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

    @Dado("que eu registro uma compra de {decimal} cotas do ativo {string} ao preço de {decimal}")
    @E("eu registro uma compra de {decimal} cotas do ativo {string} ao preço de {decimal}")
    public void registrarCompra(BigDecimal qtd, String symbol, BigDecimal preco) throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.BUY);
        request.setWalletId(walletId);
        request.setAssetId(symbol);
        request.setQuantity(qtd);
        request.setPrice(preco);
        request.setAmount(preco.multiply(qtd));
        request.setFee(BigDecimal.ZERO);
        request.setDatetime(String.format("2026-11-12T10:%02d:00", ++txCounter));

        MvcResult result = mockMvc.perform(post("/transactions")
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        createdTxIds.add(json.get("id").asText());
        if (json.has("asset") && json.get("asset").has("id")) {
            createdAssetIds.add(json.get("asset").get("id").asText());
        }
    }

    @Quando("eu consulto o resumo da carteira na moeda {string}")
    public void consultarResumoCarteira(String moeda) throws Exception {
        lastResult = mockMvc.perform(get("/wallets/{id}/summary", walletId)
                        .header("Authorization", getAuthHeader())
                        .param("currency", moeda))
                .andExpect(status().isOk())
                .andReturn();

        lastWalletSummary = objectMapper.readValue(lastResult.getResponse().getContentAsString(), WalletSummaryResponse.class);
    }

    @Então("o resumo da carteira deve conter o ativo de melhor performance {string}")
    public void validarBestPerformer(String campo) {
        assertNotNull(lastWalletSummary);
        assertNotNull(lastWalletSummary.getBestPerformer(), "bestPerformer não deve ser nulo");
        assertNotNull(lastWalletSummary.getBestPerformer().getSymbol());
        assertNotNull(lastWalletSummary.getBestPerformer().getUnrealizedProfitLossPercentage());
    }

    @E("o resumo da carteira deve conter o ativo de pior performance {string}")
    public void validarWorstPerformer(String campo) {
        assertNotNull(lastWalletSummary);
        assertNotNull(lastWalletSummary.getWorstPerformer(), "worstPerformer não deve ser nulo");
        assertNotNull(lastWalletSummary.getWorstPerformer().getSymbol());
        assertNotNull(lastWalletSummary.getWorstPerformer().getUnrealizedProfitLossPercentage());
    }

    @Quando("eu registro o recebimento de dividendos de {decimal} do ativo {string}")
    public void registrarDividendos(BigDecimal valor, String symbol) throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.DIVIDEND);
        request.setWalletId(walletId);
        request.setAssetId(symbol);
        request.setQuantity(BigDecimal.ZERO);
        request.setPrice(BigDecimal.ZERO);
        request.setAmount(valor);
        request.setFee(BigDecimal.ZERO);
        request.setDatetime(String.format("2026-11-12T10:%02d:00", ++txCounter));

        lastResult = mockMvc.perform(post("/transactions")
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        if (lastResult.getResponse().getStatus() == 201) {
            JsonNode json = objectMapper.readTree(lastResult.getResponse().getContentAsString());
            createdTxIds.add(json.get("id").asText());
            if (json.has("asset") && json.get("asset").has("id")) {
                createdAssetIds.add(json.get("asset").get("id").asText());
            }
        }
    }

    @Então("a transação de dividendo deve ser criada com status {int}")
    public void validarStatusDividendo(int statusEsperado) {
        assertEquals(statusEsperado, lastResult.getResponse().getStatus());
    }

    @E("o lucro realizado total da carteira deve incluir o valor de {decimal}")
    public void validarLucroRealizado(BigDecimal valorMinimo) throws Exception {
        consultarResumoCarteira("USD");
        assertNotNull(lastWalletSummary);
        assertNotNull(lastWalletSummary.getRealizedProfitLoss());
        assertTrue(lastWalletSummary.getRealizedProfitLoss().compareTo(valorMinimo) >= 0,
                "Lucro realizado deveria conter ao menos o dividendo recebido");
    }

    @Quando("eu consulto o gráfico histórico da carteira para o período {string} na moeda {string}")
    public void consultarGraficoCarteira(String range, String moeda) throws Exception {
        lastResult = mockMvc.perform(get("/wallets/{id}/chart", walletId)
                        .header("Authorization", getAuthHeader())
                        .param("range", range)
                        .param("currency", moeda))
                .andReturn();

        if (lastResult.getResponse().getStatus() == 200) {
            lastChartResponse = objectMapper.readValue(lastResult.getResponse().getContentAsString(), PortfolioChartResponse.class);
        }
    }

    @Então("a resposta do gráfico deve retornar status {int}")
    public void validarStatusGrafico(int statusEsperado) {
        assertEquals(statusEsperado, lastResult.getResponse().getStatus());
    }

    @E("o gráfico deve conter o período {string} e a moeda {string}")
    public void validarMetadadosGrafico(String rangeEsperado, String moedaEsperada) {
        assertNotNull(lastChartResponse);
        assertEquals(rangeEsperado, lastChartResponse.getRange());
        assertEquals(moedaEsperada, lastChartResponse.getCurrency());
    }

    @E("o gráfico deve conter uma série de pontos com timestamp e saldo positivo")
    public void validarPontosGrafico() {
        assertNotNull(lastChartResponse);
        assertNotNull(lastChartResponse.getPoints());
        assertFalse(lastChartResponse.getPoints().isEmpty(), "O gráfico deve conter pontos");
        assertTrue(lastChartResponse.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(lastChartResponse.getChangeAmount());
    }

    @Quando("eu consulto o gráfico histórico do portfólio para o período {string} na moeda {string}")
    public void consultarGraficoPortfolio(String range, String moeda) throws Exception {
        lastResult = mockMvc.perform(get("/portfolio/chart")
                        .header("Authorization", getAuthHeader())
                        .param("range", range)
                        .param("currency", moeda))
                .andReturn();

        if (lastResult.getResponse().getStatus() == 200) {
            lastChartResponse = objectMapper.readValue(lastResult.getResponse().getContentAsString(), PortfolioChartResponse.class);
        }
    }

    @E("o gráfico consolidado deve conter pontos históricos e saldo atual compatível")
    public void validarGraficoConsolidado() {
        assertNotNull(lastChartResponse);
        assertNotNull(lastChartResponse.getPoints());
        assertFalse(lastChartResponse.getPoints().isEmpty());
        assertTrue(lastChartResponse.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0);
    }

    @After
    public void cleanup() {
        try {
            for (String txId : createdTxIds) {
                mockMvc.perform(delete("/transactions/{id}", txId).header("Authorization", getAuthHeader()));
            }
            if (walletId != null) {
                mockMvc.perform(delete("/wallets/{id}", walletId).header("Authorization", getAuthHeader()));
            }
            for (String assetId : createdAssetIds) {
                mockMvc.perform(delete("/assets/{id}", assetId).header("Authorization", getAuthHeader()));
            }
            if (userId != null) {
                mockMvc.perform(delete("/users/{id}", userId).header("Authorization", getAuthHeader()));
            }
        } catch (Exception e) {
            log.warn("Erro no cleanup do cenário Fase 5: {}", e.getMessage());
        }
    }
}
