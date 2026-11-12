package dev.guilhermealves.assets.portfolio.api.app.adapters.out.market;

import com.fasterxml.jackson.databind.JsonNode;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.MarketPriceResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import dev.guilhermealves.assets.portfolio.api.app.ports.out.MarketPriceProviderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
public class BinanceMarketDataAdapter implements MarketPriceProviderPort {

    private final RestClient binanceGlobalClient;
    private final RestClient binanceUsClient;
    private volatile boolean preferUsEndpoint = false;

    public BinanceMarketDataAdapter() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(3).toMillis());

        this.binanceGlobalClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://api.binance.com")
                .build();

        this.binanceUsClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://api.binance.us")
                .build();
    }

    @Override
    public Optional<MarketPriceResponse> getPrice(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return Optional.empty();
        }

        String base = normalizeSymbol(symbol);

        // Se já foi detectada restrição geográfica americana (HTTP 451/403), consulta direto na Binance US
        if (preferUsEndpoint) {
            Optional<MarketPriceResponse> usResult = tryFetchFromBinanceUs(base);
            if (usResult.isPresent()) {
                return usResult;
            }
        }

        // Tenta Binance Global
        try {
            String pair = base + "USDT";
            JsonNode response = binanceGlobalClient.get()
                    .uri("/api/v3/ticker/24hr?symbol={symbol}", pair)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("lastPrice")) {
                return Optional.of(buildResponse(base, response, "BINANCE"));
            }
        } catch (HttpClientErrorException e) {
            // HTTP 451 (Unavailable For Legal Reasons) ou 403 ocorre quando o IP é de um VPS nos EUA
            if (e.getStatusCode().value() == 451 || e.getStatusCode().value() == 403) {
                log.warn("Binance Global bloqueada por restrição de IP/geolocalização (HTTP {} - VPS nos EUA detectada). Alternando para Binance US...", e.getStatusCode().value());
                preferUsEndpoint = true;
                return tryFetchFromBinanceUs(base);
            }
            log.warn("Erro cliente na Binance Global para {}: {}", base, e.getMessage());
        } catch (Exception e) {
            log.warn("Binance Global indisponível para {}: {}. Tentando Binance US como contingência...", base, e.getMessage());
            return tryFetchFromBinanceUs(base);
        }

        // Se não conseguiu na Binance Global, tenta Binance US
        return tryFetchFromBinanceUs(base);
    }

    private Optional<MarketPriceResponse> tryFetchFromBinanceUs(String base) {
        // Tenta primeiro o par com USDT, depois com USD
        for (String quote : new String[]{"USDT", "USD"}) {
            String pair = base + quote;
            try {
                JsonNode response = binanceUsClient.get()
                        .uri("/api/v3/ticker/24hr?symbol={symbol}", pair)
                        .retrieve()
                        .body(JsonNode.class);

                if (response != null && response.has("lastPrice")) {
                    log.debug("Cotação obtida via Binance US para o par {}", pair);
                    return Optional.of(buildResponse(base, response, "BINANCE_US"));
                }
            } catch (Exception e) {
                log.debug("Binance US não encontrou par {}: {}", pair, e.getMessage());
            }
        }
        return Optional.empty();
    }

    private MarketPriceResponse buildResponse(String symbol, JsonNode node, String source) {
        BigDecimal lastPrice = new BigDecimal(node.get("lastPrice").asText());
        BigDecimal change24h = node.has("priceChangePercent")
                ? new BigDecimal(node.get("priceChangePercent").asText()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return MarketPriceResponse.builder()
                .symbol(symbol.toUpperCase())
                .name(symbol.toUpperCase())
                .type(AssetType.CRYPTO)
                .price(lastPrice.setScale(2, RoundingMode.HALF_UP))
                .priceInUsd(lastPrice.setScale(2, RoundingMode.HALF_UP))
                .change24hPercent(change24h)
                .currency("USD")
                .source(source)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Tenta buscar a taxa USDT/BRL caso a Binance Global esteja disponível no ambiente.
     */
    public Optional<BigDecimal> fetchUsdtBrlRate() {
        if (preferUsEndpoint) {
            return Optional.empty(); // Na Binance US não existe BRL
        }

        try {
            JsonNode response = binanceGlobalClient.get()
                    .uri("/api/v3/ticker/price?symbol=USDTBRL")
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("price")) {
                BigDecimal price = new BigDecimal(response.get("price").asText()).setScale(4, RoundingMode.HALF_UP);
                log.debug("Binance USDT/BRL rate: {}", price);
                return Optional.of(price);
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 451 || e.getStatusCode().value() == 403) {
                preferUsEndpoint = true;
            }
        } catch (Exception e) {
            log.debug("Não foi possível buscar USDT/BRL na Binance: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(AssetType type) {
        return type == null || type == AssetType.CRYPTO;
    }

    @Override
    public String getProviderName() {
        return preferUsEndpoint ? "BINANCE_US" : "BINANCE";
    }

    @Override
    public int getPriority() {
        return 1;
    }

    private String normalizeSymbol(String rawSymbol) {
        String s = rawSymbol.trim().toUpperCase();
        if (s.endsWith("USDT") || s.endsWith("BUSD") || s.endsWith("USDC") || s.endsWith("USD")) {
            return s.replaceAll("(USDT|BUSD|USDC|USD)$", "");
        }
        return s;
    }
}
