package dev.guilhermealves.assets.portfolio.api.app.adapters.out.market;

import com.fasterxml.jackson.databind.JsonNode;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.MarketPriceResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import dev.guilhermealves.assets.portfolio.api.app.ports.out.MarketPriceProviderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
public class BrapiMarketDataAdapter implements MarketPriceProviderPort {

    private final RestClient restClient;

    public BrapiMarketDataAdapter() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(3).toMillis());

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://brapi.dev")
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
                    .uri("/api/quote/{ticker}", ticker)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("results")) {
                JsonNode results = response.get("results");
                if (results.isArray() && !results.isEmpty()) {
                    JsonNode quote = results.get(0);
                    if (quote.has("regularMarketPrice") && !quote.get("regularMarketPrice").isNull()) {
                        BigDecimal priceBrl = new BigDecimal(quote.get("regularMarketPrice").asText());
                        BigDecimal change = quote.has("regularMarketChangePercent") && !quote.get("regularMarketChangePercent").isNull()
                                ? new BigDecimal(quote.get("regularMarketChangePercent").asText()).setScale(2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;
                        String name = quote.has("longName") && !quote.get("longName").isNull()
                                ? quote.get("longName").asText()
                                : ticker;

                        MarketPriceResponse priceResponse = MarketPriceResponse.builder()
                                .symbol(ticker)
                                .name(name)
                                .type(AssetType.STOCK_BR)
                                .price(priceBrl.setScale(2, RoundingMode.HALF_UP))
                                .priceInBrl(priceBrl.setScale(2, RoundingMode.HALF_UP))
                                .change24hPercent(change)
                                .currency("BRL")
                                .source(getProviderName())
                                .updatedAt(LocalDateTime.now())
                                .build();

                        log.debug("Brapi price for {}: BRL {}", ticker, priceBrl);
                        return Optional.of(priceResponse);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Brapi API error for ticker {}: {}", ticker, e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public boolean supports(AssetType type) {
        return type == AssetType.STOCK_BR || type == AssetType.FII;
    }

    @Override
    public String getProviderName() {
        return "BRAPI";
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
