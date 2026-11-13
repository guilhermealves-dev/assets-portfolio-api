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
import java.util.Iterator;
import java.util.Optional;

/**
 * Provedor de cotação via exchange Kraken.
 * Usado como redundância e contingência internacional de alta confiabilidade para criptomoedas
 * clássicas como Monero (XMR), Bitcoin (BTC), Ethereum (ETH), etc.
 */
@Slf4j
@Component
public class KrakenMarketDataAdapter implements MarketPriceProviderPort {

    private final RestClient restClient;

    public KrakenMarketDataAdapter() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(3).toMillis());

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://api.kraken.com")
                .build();
    }

    @Override
    public Optional<MarketPriceResponse> getPrice(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return Optional.empty();
        }

        String base = normalizeSymbol(symbol);

        // Kraken aceita pares como XMRUSD, XMRUSDT
        for (String quote : new String[]{"USD", "USDT"}) {
            String pair = base + quote;
            try {
                JsonNode response = restClient.get()
                        .uri("/0/public/Ticker?pair={pair}", pair)
                        .retrieve()
                        .body(JsonNode.class);

                if (response != null && response.has("result") && response.path("error").isEmpty()) {
                    JsonNode result = response.get("result");
                    Iterator<String> fieldNames = result.fieldNames();

                    if (fieldNames.hasNext()) {
                        String tickerKey = fieldNames.next();
                        JsonNode tickerData = result.get(tickerKey);

                        if (tickerData.has("c") && tickerData.get("c").isArray() && !tickerData.get("c").isEmpty()) {
                            BigDecimal lastPrice = new BigDecimal(tickerData.get("c").get(0).asText());
                            BigDecimal change24h = BigDecimal.ZERO;

                            if (tickerData.has("o")) {
                                BigDecimal openPrice = new BigDecimal(tickerData.get("o").asText());
                                if (openPrice.compareTo(BigDecimal.ZERO) > 0) {
                                    change24h = lastPrice.subtract(openPrice)
                                            .divide(openPrice, 4, RoundingMode.HALF_UP)
                                            .multiply(BigDecimal.valueOf(100))
                                            .setScale(2, RoundingMode.HALF_UP);
                                }
                            }

                            MarketPriceResponse priceResponse = MarketPriceResponse.builder()
                                    .symbol(base.toUpperCase())
                                    .name(base.toUpperCase())
                                    .type(AssetType.CRYPTO)
                                    .price(lastPrice.setScale(2, RoundingMode.HALF_UP))
                                    .priceInUsd(lastPrice.setScale(2, RoundingMode.HALF_UP))
                                    .change24hPercent(change24h)
                                    .currency("USD")
                                    .source(getProviderName())
                                    .updatedAt(LocalDateTime.now())
                                    .build();

                            log.info("Cotação obtida via Kraken para {}: USD {}", base, lastPrice);
                            return Optional.of(priceResponse);
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Kraken não encontrou cotação para o par {}: {}", pair, e.getMessage());
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean supports(AssetType type) {
        return type == null || type == AssetType.CRYPTO;
    }

    @Override
    public String getProviderName() {
        return "KRAKEN";
    }

    @Override
    public int getPriority() {
        return 5;
    }

    private String normalizeSymbol(String rawSymbol) {
        String s = rawSymbol.trim().toUpperCase();
        if (s.endsWith("USDT") || s.endsWith("BUSD") || s.endsWith("USDC") || s.endsWith("USD")) {
            return s.replaceAll("(USDT|BUSD|USDC|USD)$", "");
        }
        return s;
    }
}
