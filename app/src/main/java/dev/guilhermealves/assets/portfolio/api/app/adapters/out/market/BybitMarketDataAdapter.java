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
public class BybitMarketDataAdapter implements MarketPriceProviderPort {

    private final RestClient restClient;

    public BybitMarketDataAdapter() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(3).toMillis());

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://api.bybit.com")
                .build();
    }

    @Override
    public Optional<MarketPriceResponse> getPrice(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return Optional.empty();
        }

        String pair = normalizeSymbol(symbol) + "USDT";

        try {
            JsonNode response = restClient.get()
                    .uri("/v5/market/tickers?category=spot&symbol={symbol}", pair)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("result") && response.get("result").has("list")) {
                JsonNode list = response.get("result").get("list");
                if (list.isArray() && !list.isEmpty()) {
                    JsonNode ticker = list.get(0);
                    if (ticker.has("lastPrice")) {
                        BigDecimal lastPrice = new BigDecimal(ticker.get("lastPrice").asText());
                        BigDecimal change24h = BigDecimal.ZERO;
                        if (ticker.has("price24hPcnt")) {
                            change24h = new BigDecimal(ticker.get("price24hPcnt").asText())
                                    .multiply(BigDecimal.valueOf(100))
                                    .setScale(2, RoundingMode.HALF_UP);
                        }

                        MarketPriceResponse priceResponse = MarketPriceResponse.builder()
                                .symbol(symbol.toUpperCase())
                                .name(symbol.toUpperCase())
                                .type(AssetType.CRYPTO)
                                .price(lastPrice.setScale(2, RoundingMode.HALF_UP))
                                .priceInUsd(lastPrice.setScale(2, RoundingMode.HALF_UP))
                                .change24hPercent(change24h)
                                .currency("USD")
                                .source(getProviderName())
                                .updatedAt(LocalDateTime.now())
                                .build();

                        log.debug("Bybit price for {}: USD {}", symbol, lastPrice);
                        return Optional.of(priceResponse);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Bybit API error for pair {}: {}", pair, e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public boolean supports(AssetType type) {
        return type == null || type == AssetType.CRYPTO;
    }

    @Override
    public String getProviderName() {
        return "BYBIT";
    }

    @Override
    public int getPriority() {
        return 3;
    }

    private String normalizeSymbol(String rawSymbol) {
        String s = rawSymbol.trim().toUpperCase();
        if (s.endsWith("USDT") || s.endsWith("BUSD") || s.endsWith("USDC")) {
            return s.replaceAll("(USDT|BUSD|USDC)$", "");
        }
        return s;
    }
}
