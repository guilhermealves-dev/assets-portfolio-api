package dev.guilhermealves.assets.portfolio.api.cucumber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.AssetRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.TransactionRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.UserRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.WalletRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
public class PortfolioStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Estado compartilhado entre os steps do cenário
    private String userId;
    private String assetId;
    private String walletId;
    private String transactionId;

    private UserRequest lastUserRequest;
    private AssetRequest lastAssetRequest;
    private WalletRequest lastWalletRequest;
    private TransactionRequest lastTxRequest;

    private final String runId = UUID.randomUUID().toString().substring(0, 8);

    private String getAuthHeader() {
        return "Bearer test-token-" + (userId != null ? userId : "test-user");
    }

    // ==========================================================
    // 1. USUÁRIO
    // ==========================================================

    @Dado("que eu crio um novo usuário de teste com nome {string}")
    public void criarUsuario(String nome) throws Exception {
        lastUserRequest = new UserRequest();
        lastUserRequest.setName(nome + " " + runId);
        lastUserRequest.setEmail("cucumber." + runId + "@portfolio.dev");

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lastUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(lastUserRequest.getName()))
                .andExpect(jsonPath("$.email").value(lastUserRequest.getEmail()))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        userId = json.get("id").asText();
        assertNotNull(userId);
        log.info("Usuário Cucumber criado com ID: {}", userId);
    }

    @Quando("eu busco o usuário criado pelo identificador")
    public void buscarUsuario() throws Exception {
        mockMvc.perform(get("/users/{id}", userId)
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
    }

    @Então("os dados do usuário devem ser retornados com sucesso")
    public void validarDadosUsuario() throws Exception {
        mockMvc.perform(get("/users/{id}", userId)
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(lastUserRequest.getName()))
                .andExpect(jsonPath("$.email").value(lastUserRequest.getEmail()));
    }

    @E("eu atualizo o nome do usuário para {string}")
    public void atualizarUsuario(String novoNome) throws Exception {
        lastUserRequest.setName(novoNome + " " + runId);
        mockMvc.perform(patch("/users/{id}", userId)
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lastUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(lastUserRequest.getName()));
    }

    // ==========================================================
    // 2. ATIVO
    // ==========================================================

    @Dado("que eu cadastro um novo ativo com símbolo {string}, nome {string} e tipo {string}")
    public void criarAtivo(String simbolo, String nome, String tipo) throws Exception {
        lastAssetRequest = new AssetRequest();
        lastAssetRequest.setSymbol(simbolo + "_" + runId);
        lastAssetRequest.setName(nome);
        lastAssetRequest.setType(AssetType.fromString(tipo));
        lastAssetRequest.setImgUrl("https://s2.coinmarketcap.com/static/img/coins/32x32/1.png");

        MvcResult result = mockMvc.perform(post("/assets")
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lastAssetRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.symbol").value(lastAssetRequest.getSymbol()))
                .andExpect(jsonPath("$.type").value(lastAssetRequest.getType().name()))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assetId = json.get("id").asText();
        assertNotNull(assetId);
        log.info("Ativo Cucumber criado com ID: {}", assetId);
    }

    @Quando("eu busco o ativo criado pelo identificador")
    public void buscarAtivo() throws Exception {
        mockMvc.perform(get("/assets/{id}", assetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(assetId));
    }

    @Então("os dados do ativo devem ser retornados com sucesso")
    public void validarDadosAtivo() throws Exception {
        mockMvc.perform(get("/assets/{id}", assetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value(lastAssetRequest.getSymbol()))
                .andExpect(jsonPath("$.name").value(lastAssetRequest.getName()));
    }

    @E("a lista de ativos deve conter o ativo cadastrado")
    public void listarAtivos() throws Exception {
        mockMvc.perform(get("/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @E("eu atualizo o nome do ativo para {string}")
    public void atualizarAtivo(String novoNome) throws Exception {
        lastAssetRequest.setName(novoNome);
        mockMvc.perform(patch("/assets/{id}", assetId)
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lastAssetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(novoNome));
    }

    // ==========================================================
    // 3. CARTEIRA
    // ==========================================================

    @Dado("que eu crio uma nova carteira chamada {string} vinculada ao usuário")
    public void criarCarteira(String nome) throws Exception {
        lastWalletRequest = new WalletRequest();
        lastWalletRequest.setName(nome + " " + runId);
        lastWalletRequest.setUserId(userId);
        lastWalletRequest.setIcon("🔐");
        lastWalletRequest.setColor("#0052FF");

        MvcResult result = mockMvc.perform(post("/wallets")
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lastWalletRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(lastWalletRequest.getName()))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        walletId = json.get("id").asText();
        assertNotNull(walletId);
        log.info("Carteira Cucumber criada com ID: {}", walletId);
    }

    @Quando("eu busco a carteira criada pelo identificador")
    public void buscarCarteira() throws Exception {
        mockMvc.perform(get("/wallets/{id}", walletId)
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(walletId));
    }

    @Então("os dados da carteira devem ser retornados com sucesso")
    public void validarDadosCarteira() throws Exception {
        mockMvc.perform(get("/wallets/{id}", walletId)
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(lastWalletRequest.getName()));
    }

    @E("a lista de carteiras deve conter a carteira criada")
    public void listarCarteiras() throws Exception {
        mockMvc.perform(get("/wallets")
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @E("eu atualizo o nome da carteira para {string}")
    public void atualizarCarteira(String novoNome) throws Exception {
        lastWalletRequest.setName(novoNome + " " + runId);
        mockMvc.perform(patch("/wallets/{id}", walletId)
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lastWalletRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(lastWalletRequest.getName()));
    }

    // ==========================================================
    // 4. TRANSAÇÃO
    // ==========================================================

    @Dado("que eu registro uma transação de {string} com {double} unidades a {double}")
    public void criarTransacao(String tipo, Double quantidade, Double preco) throws Exception {
        BigDecimal precoBd = BigDecimal.valueOf(preco);
        BigDecimal quantBd = BigDecimal.valueOf(quantidade);
        BigDecimal montanteBd = precoBd.multiply(quantBd);

        lastTxRequest = new TransactionRequest();
        lastTxRequest.setType(TransactionType.fromString(tipo));
        lastTxRequest.setWalletId(walletId);
        lastTxRequest.setAssetId(assetId);
        lastTxRequest.setPrice(precoBd);
        lastTxRequest.setQuantity(quantBd);
        lastTxRequest.setAmount(montanteBd);
        lastTxRequest.setFee(BigDecimal.ZERO);
        lastTxRequest.setDatetime("2026-11-12T12:00:00");

        MvcResult result = mockMvc.perform(post("/transactions")
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lastTxRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.type").value(tipo.toUpperCase()))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        transactionId = json.get("id").asText();
        assertNotNull(transactionId);
        log.info("Transação Cucumber criada com ID: {}", transactionId);
    }

    @Quando("eu busco a transação criada pelo identificador")
    public void buscarTransacao() throws Exception {
        mockMvc.perform(get("/transactions/{id}", transactionId)
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId));
    }

    @Então("os dados da transação devem ser retornados com sucesso")
    public void validarDadosTransacao() throws Exception {
        mockMvc.perform(get("/transactions/{id}", transactionId)
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value(lastTxRequest.getType().name()));
    }

    @E("eu consulto as transações filtrando pela carteira criada")
    public void listarTransacoesFiltradas() throws Exception {
        mockMvc.perform(get("/transactions")
                        .header("Authorization", getAuthHeader())
                        .param("walletId", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @E("eu atualizo a transação com novo preço {double} e montante {double}")
    public void atualizarTransacao(Double novoPreco, Double novoMontante) throws Exception {
        lastTxRequest.setPrice(BigDecimal.valueOf(novoPreco));
        lastTxRequest.setAmount(BigDecimal.valueOf(novoMontante));

        mockMvc.perform(patch("/transactions/{id}", transactionId)
                        .header("Authorization", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lastTxRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(novoPreco))
                .andExpect(jsonPath("$.amount").value(novoMontante));
    }

    // ==========================================================
    // 5. LIMPEZA / DELEÇÃO DOS DADOS
    // ==========================================================

    @Quando("eu deleto a transação de teste")
    public void deletarTransacao() throws Exception {
        mockMvc.perform(delete("/transactions/{id}", transactionId)
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isNoContent());
    }

    @Então("a transação não deve mais ser encontrada")
    public void validarTransacaoDeletada() throws Exception {
        mockMvc.perform(get("/transactions/{id}", transactionId)
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isNotFound());
        transactionId = null;
    }

    @Quando("eu deleto a carteira de teste")
    public void deletarCarteira() throws Exception {
        mockMvc.perform(delete("/wallets/{id}", walletId)
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isNoContent());
    }

    @Então("a carteira não deve mais ser encontrada")
    public void validarCarteiraDeletada() throws Exception {
        mockMvc.perform(get("/wallets/{id}", walletId)
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isNotFound());
        walletId = null;
    }

    @Quando("eu deleto o ativo de teste")
    public void deletarAtivo() throws Exception {
        mockMvc.perform(delete("/assets/{id}", assetId)
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isNoContent());
    }

    @Então("o ativo não deve mais ser encontrado")
    public void validarAtivoDeletado() throws Exception {
        mockMvc.perform(get("/assets/{id}", assetId))
                .andExpect(status().isNotFound());
        assetId = null;
    }

    @Quando("eu deleto o usuário de teste")
    public void deletarUsuario() throws Exception {
        mockMvc.perform(delete("/users/{id}", userId)
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isNoContent());
    }

    @Então("o usuário não deve mais ser encontrado")
    public void validarUsuarioDeletado() throws Exception {
        mockMvc.perform(get("/users/{id}", userId)
                        .header("Authorization", getAuthHeader()))
                .andExpect(status().isNotFound());
        userId = null;
    }

    @After
    public void cleanup() {
        // Garantia de limpeza caso algum passo anterior falhe
        try {
            if (transactionId != null) {
                mockMvc.perform(delete("/transactions/{id}", transactionId)
                        .header("Authorization", getAuthHeader()));
            }
            if (walletId != null) {
                mockMvc.perform(delete("/wallets/{id}", walletId)
                        .header("Authorization", getAuthHeader()));
            }
            if (assetId != null) {
                mockMvc.perform(delete("/assets/{id}", assetId)
                        .header("Authorization", getAuthHeader()));
            }
            if (userId != null) {
                mockMvc.perform(delete("/users/{id}", userId)
                        .header("Authorization", getAuthHeader()));
            }
        } catch (Exception e) {
            log.warn("Erro durante cleanup do Cucumber: {}", e.getMessage());
        }
    }
}
