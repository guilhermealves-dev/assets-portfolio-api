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

/**
 * Provedor de cotação via exchange KuCoin.
 * Usado como contingência para criptomoedas que foram deslistadas da Binance (ex: Monero XMR)
 * ou que possuem alta liquidez na KuCoin.
 */
@Slf4j
@Component
public class KuCoinMarketDataAdapter implements MarketPriceProviderPort {

    private final RestClient restClient;

    public KuCoinMarketDataAdapter() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(3).toMillis());

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://api.kucoin.com")
                .build();
    }

    @Override
    public Optional<MarketPriceResponse> getPrice(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return Optional.empty();
        }

        String base = normalizeSymbol(symbol);

        // Tenta par com USDT e depois com USD
        for (String quote : new String[]{"USDT", "USD"}) {
            String pair = base + "-" + quote;
            try {
                JsonNode response = restClient.get()
                        .uri("/api/v1/market/stats?symbol={symbol}", pair)
                        .retrieve()
                        .body(JsonNode.class);

                if (response != null && "200000".equals(response.path("code").asText()) && response.has("data")) {
                    JsonNode data = response.get("data");
                    if (data.has("last") && !data.get("last").isNull()) {
                        BigDecimal lastPrice = new BigDecimal(data.get("last").asText());
                        BigDecimal change24h = BigDecimal.ZERO;

                        if (data.has("changeRate") && !data.get("changeRate").isNull()) {
                            change24h = new BigDecimal(data.get("changeRate").asText())
                                    .multiply(BigDecimal.valueOf(100))
                                    .setScale(2, RoundingMode.HALF_UP);
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

                        log.info("Cotação obtida via KuCoin para {}: USD {}", base, lastPrice);
                        return Optional.of(priceResponse);
                    }
                }
            } catch (Exception e) {
                log.debug("KuCoin não encontrou cotação para o par {}: {}", pair, e.getMessage());
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
        return "KUCOIN";
    }

    @Override
    public int getPriority() {
        return 4;
    }

    private String normalizeSymbol(String rawSymbol) {
        String s = rawSymbol.trim().toUpperCase();
        if (s.endsWith("USDT") || s.endsWith("BUSD") || s.endsWith("USDC") || s.endsWith("USD")) {
            return s.replaceAll("(USDT|BUSD|USDC|USD)$", "");
        }
        return s;
    }
}
