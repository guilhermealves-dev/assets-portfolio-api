package dev.guilhermealves.assets.portfolio.api.cucumber;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.AssetSearchResultResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.ExchangeRateResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.MarketPriceResponse;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class MarketDataStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult lastResult;
    private MarketPriceResponse lastPriceResponse;
    private ExchangeRateResponse lastExchangeRateResponse;
    private Map<String, MarketPriceResponse> lastBatchPrices;
    private List<AssetSearchResultResponse> lastSearchResults;

    @Quando("eu consulto a cotação em tempo real da criptomoeda {string} na moeda {string}")
    public void consultarCotacaoCripto(String simbolo, String moeda) throws Exception {
        lastResult = mockMvc.perform(get("/market/prices/{symbol}", simbolo)
                        .param("type", "CRYPTO")
                        .param("currency", moeda))
                .andReturn();

        if (lastResult.getResponse().getStatus() == 200) {
            lastPriceResponse = objectMapper.readValue(lastResult.getResponse().getContentAsString(), MarketPriceResponse.class);
        }
    }

    @Então("a cotação deve retornar com status {int}")
    public void validarStatusCotacao(Integer statusEsperado) {
        assertEquals(statusEsperado.intValue(), lastResult.getResponse().getStatus());
    }

    @E("o preço retornado em {string} deve ser positivo")
    public void validarPrecoPositivo(String moeda) {
        assertNotNull(lastPriceResponse);
        assertEquals(moeda, lastPriceResponse.getCurrency());
        assertNotNull(lastPriceResponse.getPrice());
        assertTrue(lastPriceResponse.getPrice().compareTo(BigDecimal.ZERO) > 0, "O preço deve ser maior que zero");
    }

    @E("o provedor da cotação deve ser uma exchange reconhecida")
    public void validarProvedorReconhecido() {
        assertNotNull(lastPriceResponse);
        assertNotNull(lastPriceResponse.getSource());
        Set<String> exchangesReconhecidas = Set.of("BINANCE", "COINBASE", "BYBIT");
        assertTrue(exchangesReconhecidas.contains(lastPriceResponse.getSource()),
                "Provedor deve ser uma exchange reconhecida (Binance, Coinbase ou Bybit). Recebido: " + lastPriceResponse.getSource());
    }

    @Quando("eu consulto a taxa de câmbio de {string} para {string}")
    public void consultarTaxaCambio(String from, String to) throws Exception {
        lastResult = mockMvc.perform(get("/market/exchange-rate")
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isOk())
                .andReturn();

        lastExchangeRateResponse = objectMapper.readValue(lastResult.getResponse().getContentAsString(), ExchangeRateResponse.class);
    }

    @Então("a taxa de câmbio deve ser retornada com valor positivo")
    public void validarTaxaCambioPositiva() {
        assertNotNull(lastExchangeRateResponse);
        assertNotNull(lastExchangeRateResponse.getRate());
        assertTrue(lastExchangeRateResponse.getRate().compareTo(BigDecimal.valueOf(3.0)) > 0,
                "A taxa de câmbio USD/BRL deve ser superior a 3.0");
    }

    @Quando("eu consulto a cotação em lote para os ativos {string} na moeda {string}")
    public void consultarLote(String simbolos, String moeda) throws Exception {
        lastResult = mockMvc.perform(get("/market/prices")
                        .param("symbols", simbolos)
                        .param("type", "CRYPTO")
                        .param("currency", moeda))
                .andExpect(status().isOk())
                .andReturn();

        lastBatchPrices = objectMapper.readValue(lastResult.getResponse().getContentAsString(),
                new TypeReference<Map<String, MarketPriceResponse>>() {});
    }

    @Então("a resposta deve conter cotações para os {int} ativos")
    public void validarRespostaLote(Integer qtdEsperada) {
        assertNotNull(lastBatchPrices);
        assertEquals(qtdEsperada.intValue(), lastBatchPrices.size());
        for (Map.Entry<String, MarketPriceResponse> entry : lastBatchPrices.entrySet()) {
            assertNotNull(entry.getValue().getPrice());
            assertTrue(entry.getValue().getPrice().compareTo(BigDecimal.ZERO) > 0);
        }
    }

    @Quando("eu consulto a cotação da ação brasileira {string}")
    public void consultarAcaoBrasileira(String ticker) throws Exception {
        lastResult = mockMvc.perform(get("/market/prices/{symbol}", ticker)
                        .param("type", "STOCK_BR")
                        .param("currency", "BRL"))
                .andReturn();

        if (lastResult.getResponse().getStatus() == 200) {
            lastPriceResponse = objectMapper.readValue(lastResult.getResponse().getContentAsString(), MarketPriceResponse.class);
        }
    }

    @E("a moeda da cotação deve ser {string}")
    public void validarMoedaCotacao(String moedaEsperada) {
        assertNotNull(lastPriceResponse);
        assertEquals(moedaEsperada, lastPriceResponse.getCurrency());
    }

    @E("o provedor da cotação deve ser {string}")
    public void validarProvedor(String provedorEsperado) {
        assertNotNull(lastPriceResponse);
        assertEquals(provedorEsperado, lastPriceResponse.getSource());
    }

    @Quando("eu pesquiso ativos no mercado pelo termo {string}")
    public void pesquisarAtivos(String termo) throws Exception {
        lastResult = mockMvc.perform(get("/market/search")
                        .param("query", termo))
                .andExpect(status().isOk())
                .andReturn();

        lastSearchResults = objectMapper.readValue(lastResult.getResponse().getContentAsString(),
                new TypeReference<List<AssetSearchResultResponse>>() {});
    }

    @Então("a busca deve retornar pelo menos {int} ativo com símbolo {string}")
    public void validarBuscaRetornouAtivo(Integer qtdMinima, String simboloEsperado) {
        assertNotNull(lastSearchResults);
        long matches = lastSearchResults.stream()
                .filter(a -> a.getSymbol() != null && a.getSymbol().equalsIgnoreCase(simboloEsperado))
                .count();
        assertTrue(matches >= qtdMinima, "Esperado pelo menos " + qtdMinima + " resultado com símbolo " + simboloEsperado);
    }

    @E("o ativo encontrado deve conter nome descritivo e preço atual")
    public void validarDadosAtivoEncontrado() {
        assertNotNull(lastSearchResults);
        assertFalse(lastSearchResults.isEmpty());
        AssetSearchResultResponse first = lastSearchResults.get(0);
        assertNotNull(first.getName());
        assertNotNull(first.getCurrentPrice());
        assertTrue(first.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0);
    }
}
