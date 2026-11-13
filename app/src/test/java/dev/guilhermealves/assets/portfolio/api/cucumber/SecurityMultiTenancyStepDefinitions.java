package dev.guilhermealves.assets.portfolio.api.cucumber;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.UserFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.UserDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.AssetRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.TransactionRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.UserRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.WalletRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.UserResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.WalletResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.TransactionType;
import io.cucumber.java.After;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Slf4j
public class SecurityMultiTenancyStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserFireBaseAdapter userFireBaseAdapter;

    private MvcResult lastResult;
    private final Map<String, UserInfo> users = new HashMap<>();
    private final Map<String, String> walletIdsByUser = new HashMap<>();
    private final List<String> createdAssetIds = new ArrayList<>();
    private final List<String> createdTxIds = new ArrayList<>();
    private String sharedAssetId;

    private final String runId = UUID.randomUUID().toString().substring(0, 8);

    @Data
    @AllArgsConstructor
    private static class UserInfo {
        private String id;
        private String name;
        private String email;
        private String role;
    }

    private String getAuthHeader(String userName) {
        UserInfo u = users.get(userName);
        if (u == null) {
            return "Bearer test-token-unknown";
        }
        if ("ADMIN".equalsIgnoreCase(u.getRole())) {
            return "Bearer test-token-" + u.getId() + ":admin";
        }
        return "Bearer test-token-" + u.getId();
    }

    @Quando("uma requisição anônima sem token é feita para {string}")
    public void requisicaoAnonima(String endpoint) throws Exception {
        lastResult = mockMvc.perform(get(endpoint)).andReturn();
    }

    @Então("o status da resposta deve ser {int}")
    public void validarStatusResposta(int statusEsperado) {
        assertNotNull(lastResult);
        assertEquals(statusEsperado, lastResult.getResponse().getStatus(),
                "Status inesperado. Resposta: " + getResponseContent());
    }

    @E("a resposta deve conter detalhe de erro indicando necessidade de autenticação")
    public void validarDetalheNaoAutorizado() throws Exception {
        String content = lastResult.getResponse().getContentAsString();
        assertTrue(content.contains("Autenticação necessária") || content.contains("unauthorized") || content.contains("Não autorizado"),
                "Esperado mensagem de erro de autenticação, recebido: " + content);
    }

    @Dado("que o usuário {string} está cadastrado no sistema")
    @Dado("o usuário {string} está cadastrado no sistema")
    public void cadastrarUsuario(String nome) throws Exception {
        if (!users.containsKey(nome)) {
            UserRequest request = new UserRequest();
            request.setName(nome + " " + runId);
            request.setEmail(nome.toLowerCase().replaceAll("\\s+", ".") + "." + runId + "@portfolio.dev");

            MvcResult result = mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();

            assertEquals(201, result.getResponse().getStatus());
            JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
            String id = node.get("id").asText();
            users.put(nome, new UserInfo(id, request.getName(), request.getEmail(), "NORMAL"));
        }
    }

    @Dado("que o usuário {string} está cadastrado com perfil {string}")
    public void cadastrarUsuarioComPerfil(String nome, String perfil) throws Exception {
        cadastrarUsuario(nome);
        UserInfo u = users.get(nome);
        u.setRole(perfil.toUpperCase());
        Optional<UserDocument> opDoc = userFireBaseAdapter.findById(u.getId());
        if (opDoc.isPresent()) {
            UserDocument doc = opDoc.get();
            doc.setRole(perfil.toUpperCase());
            userFireBaseAdapter.update(doc);
        }
    }

    @Quando("o usuário {string} consulta o endpoint {string}")
    public void consultarEndpointUsuario(String nome, String endpoint) throws Exception {
        lastResult = mockMvc.perform(get(endpoint)
                        .header("Authorization", getAuthHeader(nome)))
                .andReturn();
    }

    @Então("os dados do perfil retornado devem corresponder a {string}")
    public void validarDadosPerfil(String nome) throws Exception {
        UserInfo info = users.get(nome);
        assertNotNull(info);
        UserResponse response = objectMapper.readValue(lastResult.getResponse().getContentAsString(), UserResponse.class);
        assertEquals(info.getId(), response.getId());
        assertEquals(info.getName(), response.getName());
        assertEquals(info.getEmail(), response.getEmail());
    }

    @Dado("que o usuário {string} possui uma carteira chamada {string}")
    public void criarCarteiraParaUsuario(String nome, String nomeCarteira) throws Exception {
        cadastrarUsuario(nome);
        UserInfo u = users.get(nome);

        WalletRequest req = new WalletRequest();
        req.setName(nomeCarteira + " " + runId);
        req.setUserId(u.getId());
        req.setIcon("💼");
        req.setColor("#1CC88A");

        MvcResult result = mockMvc.perform(post("/wallets")
                        .header("Authorization", getAuthHeader(nome))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        assertEquals(201, result.getResponse().getStatus());
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        walletIdsByUser.put(nome, node.get("id").asText());

        // Cria também um ativo de teste caso seja necessário
        if (sharedAssetId == null) {
            AssetRequest assetReq = new AssetRequest();
            assetReq.setSymbol("SEC_BTC_" + runId);
            assetReq.setName("Bitcoin Security " + runId);
            assetReq.setType(AssetType.CRYPTO);
            assetReq.setImgUrl("https://example.com/sec.png");

            MvcResult assetRes = mockMvc.perform(post("/assets")
                            .header("Authorization", getAuthHeader(nome))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(assetReq)))
                    .andReturn();

            if (assetRes.getResponse().getStatus() == 201) {
                JsonNode aNode = objectMapper.readTree(assetRes.getResponse().getContentAsString());
                sharedAssetId = aNode.get("id").asText();
                createdAssetIds.add(sharedAssetId);
            }
        }
    }

    @Quando("o usuário {string} tenta consultar a carteira da {string}")
    public void consultarCarteiraOutroUsuario(String nomeInvasor, String nomeProprietario) throws Exception {
        String walletId = walletIdsByUser.get(nomeProprietario);
        assertNotNull(walletId, "Carteira do proprietário não encontrada");

        lastResult = mockMvc.perform(get("/wallets/{id}", walletId)
                        .header("Authorization", getAuthHeader(nomeInvasor)))
                .andReturn();
    }

    @Quando("o usuário {string} tenta atualizar o nome da carteira da {string}")
    public void atualizarCarteiraOutroUsuario(String nomeInvasor, String nomeProprietario) throws Exception {
        String walletId = walletIdsByUser.get(nomeProprietario);
        assertNotNull(walletId, "Carteira do proprietário não encontrada");

        WalletRequest req = new WalletRequest();
        req.setId(walletId);
        req.setName("Carteira Invadida");
        req.setIcon("💼");
        req.setColor("#FF0000");

        lastResult = mockMvc.perform(patch("/wallets/{id}", walletId)
                        .header("Authorization", getAuthHeader(nomeInvasor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();
    }

    @Quando("o usuário {string} tenta deletar a carteira da {string}")
    public void deletarCarteiraOutroUsuario(String nomeInvasor, String nomeProprietario) throws Exception {
        String walletId = walletIdsByUser.get(nomeProprietario);
        assertNotNull(walletId, "Carteira do proprietário não encontrada");

        lastResult = mockMvc.perform(delete("/wallets/{id}", walletId)
                        .header("Authorization", getAuthHeader(nomeInvasor)))
                .andReturn();
    }

    @Quando("o usuário {string} tenta criar uma transação na carteira da {string}")
    public void criarTransacaoCarteiraOutroUsuario(String nomeInvasor, String nomeProprietario) throws Exception {
        String walletId = walletIdsByUser.get(nomeProprietario);
        assertNotNull(walletId, "Carteira do proprietário não encontrada");

        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.BUY);
        req.setWalletId(walletId);
        req.setAssetId(sharedAssetId != null ? sharedAssetId : "dummy-asset-id");
        req.setPrice(new BigDecimal("50000.00"));
        req.setQuantity(new BigDecimal("1.0"));
        req.setAmount(new BigDecimal("50000.00"));
        req.setFee(BigDecimal.ZERO);
        req.setDatetime("2026-11-12T12:00:00");

        lastResult = mockMvc.perform(post("/transactions")
                        .header("Authorization", getAuthHeader(nomeInvasor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();
    }

    @Quando("o usuário {string} tenta consultar o resumo analítico do portfólio da {string}")
    public void consultarResumoPortfolioOutroUsuario(String nomeInvasor, String nomeProprietario) throws Exception {
        UserInfo proprietario = users.get(nomeProprietario);
        assertNotNull(proprietario, "Proprietário não encontrado");

        lastResult = mockMvc.perform(get("/portfolio/summary")
                        .header("Authorization", getAuthHeader(nomeInvasor))
                        .param("userId", proprietario.getId()))
                .andReturn();
    }

    @Quando("o usuário {string} lista suas carteiras")
    public void listarCarteirasUsuario(String nome) throws Exception {
        lastResult = mockMvc.perform(get("/wallets")
                        .header("Authorization", getAuthHeader(nome)))
                .andReturn();
    }

    @Então("a lista deve conter apenas as carteiras da {string}")
    public void validarListaCarteirasExclusivas(String nome) throws Exception {
        assertEquals(200, lastResult.getResponse().getStatus());
        List<WalletResponse> list = objectMapper.readValue(
                lastResult.getResponse().getContentAsString(),
                new TypeReference<List<WalletResponse>>() {}
        );

        UserInfo u = users.get(nome);
        assertNotNull(u);
        String expectedWalletId = walletIdsByUser.get(nome);

        assertFalse(list.isEmpty(), "A lista de carteiras não deveria estar vazia");
        for (WalletResponse w : list) {
            if (w.getUser() != null) {
                assertEquals(u.getId(), w.getUser().getId(), "Carteira pertencente a outro usuário encontrada na listagem!");
            }
        }
        assertTrue(list.stream().anyMatch(w -> w.getId().equals(expectedWalletId)),
                "A carteira criada pelo usuário não foi encontrada na lista");
    }

    @Quando("o usuário {string} tenta executar a migração {string}")
    @Quando("o usuário {string} executa a migração {string}")
    public void executarMigracao(String nome, String endpoint) throws Exception {
        lastResult = mockMvc.perform(post(endpoint)
                        .header("Authorization", getAuthHeader(nome)))
                .andReturn();
    }

    @Quando("o usuário {string} tenta listar todos os usuários do sistema")
    @Quando("o usuário {string} lista todos os usuários do sistema")
    public void listarTodosUsuarios(String nome) throws Exception {
        lastResult = mockMvc.perform(get("/users")
                        .header("Authorization", getAuthHeader(nome)))
                .andReturn();
    }

    @Quando("o usuário {string} tenta consultar os dados cadastrais do {string}")
    public void consultarDadosOutroUsuario(String nomeAtor, String nomeAlvo) throws Exception {
        UserInfo alvo = users.get(nomeAlvo);
        assertNotNull(alvo, "Usuário alvo não encontrado");
        lastResult = mockMvc.perform(get("/users/{id}", alvo.getId())
                        .header("Authorization", getAuthHeader(nomeAtor)))
                .andReturn();
    }

    @Quando("o usuário {string} tenta deletar o usuário {string}")
    public void deletarOutroUsuario(String nomeAtor, String nomeAlvo) throws Exception {
        UserInfo alvo = users.get(nomeAlvo);
        assertNotNull(alvo, "Usuário alvo não encontrado");
        lastResult = mockMvc.perform(delete("/users/{id}", alvo.getId())
                        .header("Authorization", getAuthHeader(nomeAtor)))
                .andReturn();
    }

    @E("a migração deve reportar status de sucesso")
    public void validarSucessoMigracao() throws Exception {
        JsonNode node = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        assertEquals("SUCCESS", node.get("status").asText());
        assertTrue(node.has("migratedUsers"));
    }

    @E("a lista de usuários deve conter tanto {string} quanto {string}")
    public void validarListaUsuariosContem(String user1, String user2) throws Exception {
        List<UserResponse> list = objectMapper.readValue(
                lastResult.getResponse().getContentAsString(),
                new TypeReference<List<UserResponse>>() {}
        );
        UserInfo u1 = users.get(user1);
        UserInfo u2 = users.get(user2);
        assertNotNull(u1);
        assertNotNull(u2);

        assertTrue(list.stream().anyMatch(u -> u.getId().equals(u1.getId())), "Usuário " + user1 + " não encontrado na lista");
        assertTrue(list.stream().anyMatch(u -> u.getId().equals(u2.getId())), "Usuário " + user2 + " não encontrado na lista");
    }

    @After
    public void cleanup() {
        try {
            for (String txId : createdTxIds) {
                mockMvc.perform(delete("/transactions/{id}", txId)
                        .header("Authorization", "Bearer test-token-cleanup"));
            }
            createdTxIds.clear();

            for (Map.Entry<String, String> entry : walletIdsByUser.entrySet()) {
                UserInfo u = users.get(entry.getKey());
                String authHeader = u != null ? "Bearer test-token-" + u.getId() : "Bearer test-token-cleanup";
                mockMvc.perform(delete("/wallets/{id}", entry.getValue())
                        .header("Authorization", authHeader));
            }
            walletIdsByUser.clear();

            for (String assetId : createdAssetIds) {
                mockMvc.perform(delete("/assets/{id}", assetId)
                        .header("Authorization", "Bearer test-token-cleanup"));
            }
            createdAssetIds.clear();

            for (UserInfo u : users.values()) {
                mockMvc.perform(delete("/users/{id}", u.getId())
                        .header("Authorization", "Bearer test-token-" + u.getId()));
            }
            users.clear();
        } catch (Exception e) {
            log.warn("Erro no cleanup de segurança: {}", e.getMessage());
        }
    }

    private String getResponseContent() {
        try {
            return lastResult != null ? lastResult.getResponse().getContentAsString() : "null";
        } catch (Exception e) {
            return "erro ao ler resposta: " + e.getMessage();
        }
    }
}
