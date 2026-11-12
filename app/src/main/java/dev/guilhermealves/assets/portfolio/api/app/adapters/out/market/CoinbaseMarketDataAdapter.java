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
public class CoinbaseMarketDataAdapter implements MarketPriceProviderPort {

    private final RestClient restClient;

    public CoinbaseMarketDataAdapter() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(3).toMillis());

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://api.coinbase.com")
                .build();
    }

    @Override
    public Optional<MarketPriceResponse> getPrice(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return Optional.empty();
        }

        String base = normalizeSymbol(symbol);
        String pair = base + "-USD";

        try {
            JsonNode response = restClient.get()
                    .uri("/v2/prices/{pair}/spot", pair)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("data") && response.get("data").has("amount")) {
                BigDecimal amount = new BigDecimal(response.get("data").get("amount").asText());

                MarketPriceResponse priceResponse = MarketPriceResponse.builder()
                        .symbol(base)
                        .name(base)
                        .type(AssetType.CRYPTO)
                        .price(amount.setScale(2, RoundingMode.HALF_UP))
                        .priceInUsd(amount.setScale(2, RoundingMode.HALF_UP))
                        .change24hPercent(BigDecimal.ZERO)
                        .currency("USD")
                        .source(getProviderName())
                        .updatedAt(LocalDateTime.now())
                        .build();

                log.debug("Coinbase price for {}: USD {}", base, amount);
                return Optional.of(priceResponse);
            }
        } catch (Exception e) {
            log.warn("Coinbase API error for pair {}: {}", pair, e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public boolean supports(AssetType type) {
        return type == null || type == AssetType.CRYPTO;
    }

    @Override
    public String getProviderName() {
        return "COINBASE";
    }

    @Override
    public int getPriority() {
        return 2;
    }

    private String normalizeSymbol(String rawSymbol) {
        String s = rawSymbol.trim().toUpperCase();
        if (s.endsWith("USDT") || s.endsWith("BUSD") || s.endsWith("USDC")) {
            return s.replaceAll("(USDT|BUSD|USDC)$", "");
        }
        return s;
    }
}
