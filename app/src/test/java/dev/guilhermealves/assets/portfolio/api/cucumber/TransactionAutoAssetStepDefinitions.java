package dev.guilhermealves.assets.portfolio.api.cucumber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.TransactionRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.UserRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.WalletRequest;
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
public class TransactionAutoAssetStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userId;
    private String walletId;
    private final List<String> createdTxIds = new ArrayList<>();
    private String firstTxAssetId;
    private String secondTxAssetId;
    private MvcResult lastResult;
    private JsonNode lastJson;
    private int txCounter = 0;

    private final String runId = UUID.randomUUID().toString().substring(0, 8);

    private String getAuthHeader() {
        return "Bearer test-token-" + (userId != null ? userId : "test-user");
    }

    @Dado("que eu crio um usuário autenticado para lançamento de transações")
    public void criarUsuarioTransacoes() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("Investidor Auto Provision " + runId);
        request.setEmail("autoprovision." + runId + "@portfolio.dev");

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        userId = node.get("id").asText();
        assertNotNull(userId);
    }

    @E("eu crio uma carteira de investimentos para o usuário")
    public void criarCarteira() throws Exception {
        WalletRequest request = new WalletRequest();
        request.setName("Carteira Teste " + runId);
        request.setUserId(userId);
        request.setIcon("💼");
        request.setColor("#1CC88A");

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

    @Quando("eu registro uma compra informando o assetId {string}, quantidade {decimal} e preço {decimal}")
    public void registrarCompraPorAssetId(String symbolOrId, BigDecimal quantidade, BigDecimal preco) throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.BUY);
        request.setWalletId(walletId);
        request.setAssetId(symbolOrId);
        request.setPrice(preco);
        request.setQuantity(quantidade);
        request.setAmount(preco.multiply(quantidade));
        request.setFee(BigDecimal.ZERO);
        request.setDatetime(String.format("2026-11-12T11:%02d:00", ++txCounter));

        lastResult = mockMvc.perform(post("/transactions")
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        if (lastResult.getResponse().getStatus() == 201) {
            lastJson = objectMapper.readTree(lastResult.getResponse().getContentAsString());
            String txId = lastJson.get("id").asText();
            createdTxIds.add(txId);
            if (lastJson.has("asset") && lastJson.get("asset").has("id")) {
                firstTxAssetId = lastJson.get("asset").get("id").asText();
            }
        }
    }

    @Dado("que uma transação com o ativo {string} já foi registrada")
    public void registrarTransacaoPrevia(String symbol) throws Exception {
        registrarCompraPorAssetId(symbol, BigDecimal.valueOf(1), BigDecimal.valueOf(100));
        assertNotNull(firstTxAssetId, "Ativo prévio deveria ter sido criado/resolvido");
    }

    @Quando("eu registro uma segunda compra informando o assetId {string}, quantidade {decimal} e preço {decimal}")
    public void registrarSegundaCompra(String symbolOrId, BigDecimal quantidade, BigDecimal preco) throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.BUY);
        request.setWalletId(walletId);
        request.setAssetId(symbolOrId);
        request.setPrice(preco);
        request.setQuantity(quantidade);
        request.setAmount(preco.multiply(quantidade));
        request.setFee(BigDecimal.ZERO);
        request.setDatetime(String.format("2026-11-12T11:%02d:00", ++txCounter));

        lastResult = mockMvc.perform(post("/transactions")
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        if (lastResult.getResponse().getStatus() == 201) {
            lastJson = objectMapper.readTree(lastResult.getResponse().getContentAsString());
            String txId = lastJson.get("id").asText();
            createdTxIds.add(txId);
            if (lastJson.has("asset") && lastJson.get("asset").has("id")) {
                secondTxAssetId = lastJson.get("asset").get("id").asText();
            }
        }
    }

    @Quando("eu registro uma compra informando o assetSymbol {string}, quantidade {decimal} e preço {decimal}")
    public void registrarCompraPorAssetSymbol(String symbol, BigDecimal quantidade, BigDecimal preco) throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.BUY);
        request.setWalletId(walletId);
        request.setAssetSymbol(symbol);
        request.setPrice(preco);
        request.setQuantity(quantidade);
        request.setAmount(preco.multiply(quantidade));
        request.setFee(BigDecimal.ZERO);
        request.setDatetime(String.format("2026-11-12T11:%02d:00", ++txCounter));

        lastResult = mockMvc.perform(post("/transactions")
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        if (lastResult.getResponse().getStatus() == 201) {
            lastJson = objectMapper.readTree(lastResult.getResponse().getContentAsString());
            String txId = lastJson.get("id").asText();
            createdTxIds.add(txId);
        }
    }

    @Quando("eu tento registrar uma transação sem informar assetId e sem assetSymbol")
    public void tentarRegistrarTransacaoSemAtivo() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.BUY);
        request.setWalletId(walletId);
        request.setPrice(BigDecimal.valueOf(100));
        request.setQuantity(BigDecimal.valueOf(1));
        request.setAmount(BigDecimal.valueOf(100));
        request.setFee(BigDecimal.ZERO);
        request.setDatetime("2026-11-12T11:00:00");

        lastResult = mockMvc.perform(post("/transactions")
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();
    }

    @Então("a transação deve ser criada com status {int}")
    public void validarStatusTransacao(int statusEsperado) {
        assertEquals(statusEsperado, lastResult.getResponse().getStatus());
    }

    @E("o ativo retornado na transação deve ter símbolo {string} e tipo {string}")
    public void validarAtivoRetornado(String simboloEsperado, String tipoEsperado) {
        assertNotNull(lastJson, "JSON de resposta não deve ser nulo");
        assertTrue(lastJson.has("asset"), "Resposta deve conter o objeto 'asset'");
        JsonNode assetNode = lastJson.get("asset");
        assertEquals(simboloEsperado, assetNode.get("symbol").asText());
        assertEquals(tipoEsperado, assetNode.get("type").asText());
    }

    @E("o ativo retornado na transação deve ter um identificador gerado")
    public void validarIdAtivoGerado() {
        assertNotNull(lastJson);
        assertTrue(lastJson.has("asset"));
        String assetId = lastJson.get("asset").get("id").asText();
        assertNotNull(assetId);
        assertFalse(assetId.isBlank());
    }

    @E("o ativo retornado na transação deve ter um ícone funcional")
    public void validarIconeFuncional() {
        assertNotNull(lastJson);
        assertTrue(lastJson.has("asset"));
        JsonNode assetNode = lastJson.get("asset");
        assertTrue(assetNode.has("imgUrl"), "Ativo deve possuir imgUrl");
        String imgUrl = assetNode.get("imgUrl").asText();
        assertNotNull(imgUrl);
        assertFalse(imgUrl.isBlank());
        assertFalse(imgUrl.contains("coingecko.com/coins/images/"), "Não deve usar URL quebrada do CoinGecko");
        assertTrue(imgUrl.startsWith("http://") || imgUrl.startsWith("https://"));
    }

    @E("a segunda transação deve apontar para o mesmo identificador de ativo da primeira transação")
    public void validarMesmoIdAtivo() {
        assertNotNull(firstTxAssetId, "ID do primeiro ativo não pode ser nulo");
        assertNotNull(secondTxAssetId, "ID do segundo ativo não pode ser nulo");
        assertEquals(firstTxAssetId, secondTxAssetId, "O ativo deveria ser reaproveitado pelo símbolo");
    }

    @Então("a tentativa deve ser rejeitada com status {int}")
    public void validarStatusRejeicao(int statusEsperado) {
        assertEquals(statusEsperado, lastResult.getResponse().getStatus());
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
            if (userId != null) {
                mockMvc.perform(delete("/users/{id}", userId).header("Authorization", getAuthHeader()));
            }
        } catch (Exception e) {
            log.warn("Erro no cleanup do cenário TransactionAutoAsset: {}", e.getMessage());
        }
    }
}
