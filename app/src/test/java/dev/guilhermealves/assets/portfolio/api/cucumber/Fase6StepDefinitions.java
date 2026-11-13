package dev.guilhermealves.assets.portfolio.api.cucumber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.*;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.HoldingResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.TransactionImportResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.TransactionType;
import io.cucumber.java.After;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class Fase6StepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userId;
    private String userBId;
    private String walletId;
    private String sourceWalletId;
    private String targetWalletId;
    private String userBWalletId;

    private final List<String> createdTxIds = new ArrayList<>();
    private final Set<String> createdAssetIds = new HashSet<>();
    private final List<String> createdWalletIds = new ArrayList<>();
    private final List<String> createdUserIds = new ArrayList<>();
    private final Set<String> watchlistAddedSymbols = new HashSet<>();

    private MvcResult lastResult;
    private TransactionImportResponse lastImportResponse;
    private final String runId = UUID.randomUUID().toString().substring(0, 8);

    private String getAuthHeader() {
        return "Bearer test-token-" + (userId != null ? userId : "test-user");
    }

    private String getUserBAuthHeader() {
        return "Bearer test-token-" + (userBId != null ? userBId : "test-user-b");
    }

    @Dado("que a API de gestão de portfólio está em execução")
    public void apiEmExecucao() {
        assertNotNull(mockMvc);
    }

    @Dado("que eu crio um usuário autenticado para testes da Fase 6")
    public void criarUsuarioFase6() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("Investidor Fase6 " + runId);
        request.setEmail("fase6." + runId + "@portfolio.dev");

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        this.userId = node.get("id").asText();
        createdUserIds.add(this.userId);
    }

    @Quando("eu adiciono o ativo {string} à minha watchlist")
    public void adicionarAtivoWatchlist(String symbol) throws Exception {
        WatchlistRequest request = WatchlistRequest.builder()
                .assetSymbol(symbol)
                .build();

        lastResult = mockMvc.perform(post("/watchlist")
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        watchlistAddedSymbols.add(symbol);
    }

    @Então("o ativo deve ser adicionado com status {int} e conter dados de cotação atual")
    public void validarAtivoAdicionadoWatchlist(int expectedStatus) throws Exception {
        assertEquals(expectedStatus, lastResult.getResponse().getStatus());
        JsonNode node = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        assertNotNull(node.get("id"));
        assertNotNull(node.get("symbol"));
        assertNotNull(node.get("currentPrice"));
        assertTrue(node.get("currentPrice").asDouble() > 0);
    }

    @E("a verificação da presença de {string} na watchlist deve retornar verdadeiro")
    public void verificarPresencaWatchlistVerdadeiro(String symbol) throws Exception {
        MvcResult res = mockMvc.perform(get("/watchlist/check/{symbol}", symbol)
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
        assertTrue(node.get("inWatchlist").asBoolean());
    }

    @Quando("eu consulto minha watchlist na moeda {string}")
    public void consultarWatchlist(String moeda) throws Exception {
        lastResult = mockMvc.perform(get("/watchlist")
                        .header("Authorization", getAuthHeader())
                        .param("currency", moeda))
                .andReturn();
    }

    @Então("a watchlist deve conter o ativo {string} com cotação na moeda {string}")
    public void validarWatchlistComMoeda(String symbol, String moeda) throws Exception {
        assertEquals(200, lastResult.getResponse().getStatus());
        JsonNode array = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        assertTrue(array.isArray());
        assertFalse(array.isEmpty());

        boolean found = false;
        for (JsonNode item : array) {
            if (symbol.equalsIgnoreCase(item.get("symbol").asText())) {
                found = true;
                assertEquals(moeda, item.get("currency").asText());
                assertTrue(item.get("currentPrice").asDouble() > 0);
            }
        }
        assertTrue(found, "Ativo " + symbol + " não encontrado na watchlist");
    }

    @Quando("eu removo o ativo {string} da minha watchlist")
    public void removerAtivoWatchlist(String symbol) throws Exception {
        lastResult = mockMvc.perform(delete("/watchlist/{symbol}", symbol)
                        .header("Authorization", getAuthHeader()))
                .andReturn();
    }

    @Então("a remoção deve ser realizada com status {int}")
    public void validarRemocaoWatchlist(int expectedStatus) {
        assertEquals(expectedStatus, lastResult.getResponse().getStatus());
    }

    @E("a verificação da presença de {string} na watchlist deve retornar falso")
    public void verificarPresencaWatchlistFalso(String symbol) throws Exception {
        MvcResult res = mockMvc.perform(get("/watchlist/check/{symbol}", symbol)
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
        assertFalse(node.get("inWatchlist").asBoolean());
    }

    @E("eu crio uma carteira de investimentos {string} para o usuário da Fase 6")
    public void criarCarteiraFase6(String nome) throws Exception {
        WalletRequest req = new WalletRequest();
        req.setName(nome + " " + runId);
        req.setUserId(this.userId);
        req.setIcon("💼");
        req.setColor("#10B981");

        MvcResult res = mockMvc.perform(post("/wallets")
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
        String wId = node.get("id").asText();
        this.walletId = wId;
        this.sourceWalletId = wId;
        createdWalletIds.add(wId);
    }

    @E("eu crio uma segunda carteira de investimentos {string} para o usuário da Fase 6")
    public void criarSegundaCarteira(String nome) throws Exception {
        WalletRequest req = new WalletRequest();
        req.setName(nome + " " + runId);
        req.setUserId(this.userId);
        req.setIcon("🏦");
        req.setColor("#3B82F6");

        MvcResult res = mockMvc.perform(post("/wallets")
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
        String wId = node.get("id").asText();
        this.targetWalletId = wId;
        createdWalletIds.add(wId);
    }

    @E("eu registro uma compra de {decimal} cotas do ativo {string} ao preço de {decimal} na carteira")
    public void registrarCompraCarteira(BigDecimal qtd, String symbol, BigDecimal preco) throws Exception {
        registrarCompraGenerica(this.walletId, qtd, symbol, preco);
    }

    @E("eu registro uma compra de {decimal} cotas do ativo {string} ao preço de {decimal} na carteira de origem")
    public void registrarCompraCarteiraOrigem(BigDecimal qtd, String symbol, BigDecimal preco) throws Exception {
        registrarCompraGenerica(this.sourceWalletId, qtd, symbol, preco);
    }

    private void registrarCompraGenerica(String targetWId, BigDecimal qtd, String symbol, BigDecimal preco) throws Exception {
        BigDecimal amount = preco.multiply(qtd);
        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.BUY);
        req.setWalletId(targetWId);
        req.setAssetSymbol(symbol);
        req.setQuantity(qtd);
        req.setPrice(preco);
        req.setAmount(amount);
        req.setFee(BigDecimal.ZERO);
        req.setDatetime(LocalDateTime.now().toString());

        MvcResult res = mockMvc.perform(post("/transactions")
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
        createdTxIds.add(node.get("id").asText());
        if (node.has("asset") && node.get("asset").has("id")) {
            createdAssetIds.add(node.get("asset").get("id").asText());
        }
    }

    @Quando("eu solicito a exportação de transações em formato CSV")
    public void exportarCsv() throws Exception {
        lastResult = mockMvc.perform(get("/transactions/export")
                        .header("Authorization", getAuthHeader())
                        .param("walletId", this.walletId))
                .andReturn();
    }

    @Então("o arquivo retornado deve ter status {int}, tipo {string} e conter cabeçalho e os dados de {string}")
    public void validarExportacaoCsv(int expectedStatus, String expectedContentType, String symbol) throws Exception {
        assertEquals(expectedStatus, lastResult.getResponse().getStatus());
        assertTrue(lastResult.getResponse().getContentType().contains(expectedContentType));
        String csvContent = lastResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(csvContent.contains("id,datetime,type,walletId,walletName"));
        assertTrue(csvContent.contains(symbol));
    }

    @Quando("eu realizo a importação de um arquivo CSV com novas transações de {string} e {string}")
    public void importarCsv(String symbol1, String symbol2) throws Exception {
        String csvData = "type,assetsymbol,quantity,price,amount,fee,walletid\n" +
                "BUY," + symbol1 + ",0.5,3000.00,1500.00,0," + this.walletId + "\n" +
                "BUY," + symbol2 + ",10.0,60.00,600.00,0," + this.walletId + "\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "novas_transacoes.csv",
                "text/csv",
                csvData.getBytes(StandardCharsets.UTF_8)
        );

        lastResult = mockMvc.perform(multipart("/transactions/import")
                        .file(file)
                        .header("Authorization", getAuthHeader()))
                .andReturn();

        if (lastResult.getResponse().getStatus() == 200) {
            lastImportResponse = objectMapper.readValue(lastResult.getResponse().getContentAsString(), TransactionImportResponse.class);
            if (lastImportResponse.getImportedTransactions() != null) {
                lastImportResponse.getImportedTransactions().forEach(tx -> createdTxIds.add(tx.getId()));
            }
        }
    }

    @Então("o resultado da importação deve ter {int} transações processadas com sucesso e {int} falhas")
    public void validarImportacaoCsv(int expectedSuccess, int expectedFailed) {
        assertNotNull(lastImportResponse);
        assertEquals(expectedSuccess, lastImportResponse.getTotalSuccess());
        assertEquals(expectedFailed, lastImportResponse.getTotalFailed());
    }

    @Quando("eu realizo uma transferência de {decimal} cotas de {string} da carteira de origem para a carteira de destino")
    public void transferirEntreCarteiras(BigDecimal qtd, String symbol) throws Exception {
        WalletTransferRequest req = WalletTransferRequest.builder()
                .sourceWalletId(this.sourceWalletId)
                .targetWalletId(this.targetWalletId)
                .assetSymbol(symbol)
                .quantity(qtd)
                .fee(BigDecimal.ZERO)
                .build();

        lastResult = mockMvc.perform(post("/wallets/transfer")
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        if (lastResult.getResponse().getStatus() == 200) {
            JsonNode node = objectMapper.readTree(lastResult.getResponse().getContentAsString());
            if (node.has("sourceTransaction") && node.get("sourceTransaction").has("id")) {
                createdTxIds.add(node.get("sourceTransaction").get("id").asText());
            }
            if (node.has("targetTransaction") && node.get("targetTransaction").has("id")) {
                createdTxIds.add(node.get("targetTransaction").get("id").asText());
            }
        }
    }

    @Então("a transferência deve ser concluída com sucesso status {int}")
    public void validarStatusTransferencia(int expectedStatus) {
        assertEquals(expectedStatus, lastResult.getResponse().getStatus());
    }

    @E("a carteira de origem deve possuir saldo de {decimal} cotas de {string}")
    public void validarSaldoOrigem(BigDecimal expectedQtd, String symbol) throws Exception {
        validarSaldoCustodia(this.sourceWalletId, symbol, expectedQtd);
    }

    @E("a carteira de destino deve possuir saldo de {decimal} cotas de {string}")
    public void validarSaldoDestino(BigDecimal expectedQtd, String symbol) throws Exception {
        validarSaldoCustodia(this.targetWalletId, symbol, expectedQtd);
    }

    private void validarSaldoCustodia(String wId, String symbol, BigDecimal expectedQtd) throws Exception {
        MvcResult res = mockMvc.perform(get("/wallets/{id}/holdings", wId)
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode array = objectMapper.readTree(res.getResponse().getContentAsString());
        assertTrue(array.isArray());

        boolean found = false;
        for (JsonNode item : array) {
            if (item.has("asset") && symbol.equalsIgnoreCase(item.get("asset").get("symbol").asText())) {
                found = true;
                BigDecimal actualQty = new BigDecimal(item.get("quantity").asText());
                assertEquals(0, actualQty.compareTo(expectedQtd),
                        "Saldo esperado: " + expectedQtd + ", obtido: " + actualQty);
            }
        }
        assertTrue(found, "Ativo " + symbol + " não encontrado em custódia na carteira " + wId);
    }

    @E("existe um segundo usuário no sistema com uma carteira própria {string}")
    public void criarSegundoUsuarioComCarteira(String nomeCarteira) throws Exception {
        UserRequest request = new UserRequest();
        request.setName("Usuário B " + runId);
        request.setEmail("user.b." + runId + "@portfolio.dev");

        MvcResult userRes = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        this.userBId = objectMapper.readTree(userRes.getResponse().getContentAsString()).get("id").asText();
        createdUserIds.add(this.userBId);

        WalletRequest walletReq = new WalletRequest();
        walletReq.setName(nomeCarteira + " " + runId);
        walletReq.setUserId(this.userBId);
        walletReq.setIcon("💼");
        walletReq.setColor("#EF4444");

        MvcResult walletRes = mockMvc.perform(post("/wallets")
                        .header("Authorization", getUserBAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletReq)))
                .andExpect(status().isCreated())
                .andReturn();

        this.userBWalletId = objectMapper.readTree(walletRes.getResponse().getContentAsString()).get("id").asText();
        createdWalletIds.add(this.userBWalletId);
    }

    @Quando("o primeiro usuário tenta transferir cotas para a carteira do segundo usuário")
    public void tentarTransferirParaOutroUsuario() throws Exception {
        WalletTransferRequest req = WalletTransferRequest.builder()
                .sourceWalletId(this.walletId)
                .targetWalletId(this.userBWalletId)
                .assetSymbol("SOL")
                .quantity(new BigDecimal("0.5"))
                .fee(BigDecimal.ZERO)
                .build();

        lastResult = mockMvc.perform(post("/wallets/transfer")
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();
    }

    @Então("a operação de transferência deve ser rejeitada com status {int}")
    public void validarRejeicaoTransferencia(int expectedStatus) {
        assertEquals(expectedStatus, lastResult.getResponse().getStatus());
    }

    @Quando("eu realizo uma troca de {decimal} cotas de {string} por {decimal} cotas de {string}")
    public void realizarSwap(BigDecimal fromQtd, String fromSymbol, BigDecimal toQtd, String toSymbol) throws Exception {
        WalletSwapRequest req = WalletSwapRequest.builder()
                .fromAsset(fromSymbol)
                .fromQuantity(fromQtd)
                .toAsset(toSymbol)
                .toQuantity(toQtd)
                .fee(BigDecimal.ZERO)
                .build();

        lastResult = mockMvc.perform(post("/wallets/{id}/swap", this.walletId)
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        if (lastResult.getResponse().getStatus() == 200) {
            JsonNode node = objectMapper.readTree(lastResult.getResponse().getContentAsString());
            if (node.has("fromTransaction") && node.get("fromTransaction").has("id")) {
                createdTxIds.add(node.get("fromTransaction").get("id").asText());
            }
            if (node.has("toTransaction") && node.get("toTransaction").has("id")) {
                createdTxIds.add(node.get("toTransaction").get("id").asText());
            }
        }
    }

    @Então("a operação de swap deve retornar status {int} com transações de compra e venda geradas")
    public void validarOperacaoSwap(int expectedStatus) throws Exception {
        assertEquals(expectedStatus, lastResult.getResponse().getStatus());
        JsonNode node = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        assertNotNull(node.get("fromTransaction"));
        assertNotNull(node.get("toTransaction"));
        assertNotNull(node.get("realizedProfitLoss"));
    }

    @E("o saldo restante de {string} na carteira deve ser {decimal} cotas")
    public void validarSaldoRestante(String symbol, BigDecimal expectedQtd) throws Exception {
        validarSaldoCustodia(this.walletId, symbol, expectedQtd);
    }

    @After
    public void cleanup() {
        try {
            // Limpa watchlist items
            for (String symbol : watchlistAddedSymbols) {
                try {
                    mockMvc.perform(delete("/watchlist/{symbol}", symbol).header("Authorization", getAuthHeader()));
                } catch (Exception ignored) {
                }
            }
            // Limpa transações
            for (String txId : createdTxIds) {
                try {
                    mockMvc.perform(delete("/transactions/{id}", txId).header("Authorization", getAuthHeader()));
                } catch (Exception ignored) {
                }
            }
            // Limpa carteiras
            for (String wId : createdWalletIds) {
                try {
                    mockMvc.perform(delete("/wallets/{id}", wId).header("Authorization", getAuthHeader()));
                } catch (Exception ignored) {
                }
                try {
                    mockMvc.perform(delete("/wallets/{id}", wId).header("Authorization", getUserBAuthHeader()));
                } catch (Exception ignored) {
                }
            }
            // Limpa ativos
            for (String assetId : createdAssetIds) {
                try {
                    mockMvc.perform(delete("/assets/{id}", assetId).header("Authorization", getAuthHeader()));
                } catch (Exception ignored) {
                }
            }
            // Limpa usuários
            for (String uId : createdUserIds) {
                try {
                    mockMvc.perform(delete("/users/{id}", uId).header("Authorization", getAuthHeader()));
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            log.warn("Erro no cleanup do cenário Fase 6: {}", e.getMessage());
        }
    }
}
