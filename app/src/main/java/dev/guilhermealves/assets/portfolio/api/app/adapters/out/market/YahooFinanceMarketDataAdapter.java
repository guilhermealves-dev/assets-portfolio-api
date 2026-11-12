package dev.guilhermealves.assets.portfolio.api.app.adapters.out.market;

import com.fasterxml.jackson.databind.JsonNode;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.AssetSearchResultResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.MarketPriceResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import dev.guilhermealves.assets.portfolio.api.app.ports.out.MarketPriceProviderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class YahooFinanceMarketDataAdapter implements MarketPriceProviderPort {

    private final RestClient restClient;

    public YahooFinanceMarketDataAdapter() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(3).toMillis());

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://query1.finance.yahoo.com")
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build();
    }

    @Override
    public Optional<MarketPriceResponse> getPrice(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return Optional.empty();
        }

        String ticker = symbol.trim().toUpperCase();

        try {
            JsonNode response = restClient.get()
                    .uri("/v8/finance/chart/{ticker}", ticker)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("chart") && response.get("chart").has("result")) {
                JsonNode results = response.get("chart").get("result");
                if (results.isArray() && !results.isEmpty()) {
                    JsonNode meta = results.get(0).get("meta");
                    if (meta != null && meta.has("regularMarketPrice") && !meta.get("regularMarketPrice").isNull()) {
                        BigDecimal priceUsd = new BigDecimal(meta.get("regularMarketPrice").asText());
                        BigDecimal change = meta.has("regularMarketChangePercent") && !meta.get("regularMarketChangePercent").isNull()
                                ? new BigDecimal(meta.get("regularMarketChangePercent").asText()).setScale(2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;
                        String name = meta.has("longName") && !meta.get("longName").isNull()
                                ? meta.get("longName").asText()
                                : ticker;

                        MarketPriceResponse priceResponse = MarketPriceResponse.builder()
                                .symbol(ticker)
                                .name(name)
                                .type(AssetType.STOCK_US)
                                .price(priceUsd.setScale(2, RoundingMode.HALF_UP))
                                .priceInUsd(priceUsd.setScale(2, RoundingMode.HALF_UP))
                                .change24hPercent(change)
                                .currency("USD")
                                .source(getProviderName())
                                .updatedAt(LocalDateTime.now())
                                .build();

                        log.debug("Yahoo Finance price for {}: USD {}", ticker, priceUsd);
                        return Optional.of(priceResponse);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Yahoo Finance API error for ticker {}: {}", ticker, e.getMessage());
        }

        return Optional.empty();
    }

    public List<AssetSearchResultResponse> search(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        List<AssetSearchResultResponse> results = new ArrayList<>();
        try {
            JsonNode response = restClient.get()
                    .uri("/v1/finance/search?q={query}&quotesCount=5&newsCount=0", query.trim())
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("quotes") && response.get("quotes").isArray()) {
                for (JsonNode q : response.get("quotes")) {
                    String symbol = q.has("symbol") ? q.get("symbol").asText() : null;
                    if (symbol == null || symbol.contains(".")) {
                        continue;
                    }

                    String quoteType = q.has("quoteType") ? q.get("quoteType").asText() : "";
                    AssetType type = "ETF".equalsIgnoreCase(quoteType) ? AssetType.ETF : AssetType.STOCK_US;
                    String name = q.has("shortname") && !q.get("shortname").isNull() ? q.get("shortname").asText() :
                            (q.has("longname") && !q.get("longname").isNull() ? q.get("longname").asText() : symbol);

                    Optional<MarketPriceResponse> priceOpt = getPrice(symbol);
                    BigDecimal price = priceOpt.map(MarketPriceResponse::getPrice).orElse(null);

                    results.add(AssetSearchResultResponse.builder()
                            .symbol(symbol)
                            .name(name)
                            .type(type)
                            .currentPrice(price)
                            .currency("USD")
                            .origin("YAHOO")
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("Erro ao buscar ativos no Yahoo Finance: {}", e.getMessage());
        }
        return results;
    }

    @Override
    public boolean supports(AssetType type) {
        return type == AssetType.STOCK_US || type == AssetType.ETF;
    }

    @Override
    public String getProviderName() {
        return "YAHOO";
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
