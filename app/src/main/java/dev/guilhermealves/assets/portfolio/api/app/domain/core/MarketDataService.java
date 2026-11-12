package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import dev.guilhermealves.assets.portfolio.api.app.adapters.out.AssetFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.market.BinanceMarketDataAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.AssetDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.AssetSearchResultResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.ExchangeRateResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.MarketPriceResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import dev.guilhermealves.assets.portfolio.api.app.ports.out.MarketPriceProviderPort;
import dev.guilhermealves.assets.portfolio.api.config.CacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MarketDataService {

    private final List<MarketPriceProviderPort> providers;
    private final BinanceMarketDataAdapter binanceMarketDataAdapter;
    private final AssetFireBaseAdapter assetFireBaseAdapter;
    private final RestClient httpClient;

    public MarketDataService(List<MarketPriceProviderPort> providers,
                             BinanceMarketDataAdapter binanceMarketDataAdapter,
                             AssetFireBaseAdapter assetFireBaseAdapter) {
        this.binanceMarketDataAdapter = binanceMarketDataAdapter;
        this.assetFireBaseAdapter = assetFireBaseAdapter;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(3).toMillis());
        this.httpClient = RestClient.builder().requestFactory(factory).build();

        // Ordena provedores pela prioridade (menor valor = chamado primeiro)
        this.providers = providers.stream()
                .sorted(Comparator.comparingInt(MarketPriceProviderPort::getPriority))
                .collect(Collectors.toList());
    }

    /**
     * Obtém a cotação de mercado de um ativo com fallback entre exchanges e conversão para a moeda de destino.
     */
    @Cacheable(value = CacheConfig.CACHE_MARKET_PRICES, key = "#symbol.toUpperCase() + '_' + (#targetCurrency != null ? #targetCurrency.toUpperCase() : 'USD')")
    public Optional<MarketPriceResponse> getMarketPrice(String symbol, AssetType type, String targetCurrency) {
        if (symbol == null || symbol.isBlank()) {
            return Optional.empty();
        }

        String target = (targetCurrency != null && !targetCurrency.isBlank()) ? targetCurrency.toUpperCase() : "USD";
        AssetType assetType = type != null ? type : AssetType.CRYPTO;

        for (MarketPriceProviderPort provider : providers) {
            if (provider.supports(assetType)) {
                try {
                    Optional<MarketPriceResponse> responseOpt = provider.getPrice(symbol);
                    if (responseOpt.isPresent()) {
                        MarketPriceResponse response = responseOpt.get();
                        enrichWithCurrencies(response, target);
                        log.debug("Cotação obtida para {} via provedor {}", symbol, provider.getProviderName());
                        return Optional.of(response);
                    }
                } catch (Exception e) {
                    log.warn("Falha no provedor {} ao cotar {}: {}", provider.getProviderName(), symbol, e.getMessage());
                }
            }
        }

        log.warn("Nenhum provedor disponível conseguiu cotar o ativo {}", symbol);
        return Optional.empty();
    }

    /**
     * Cota múltiplos ativos em lote com a moeda de destino.
     */
    public Map<String, MarketPriceResponse> getMultiplePrices(Collection<String> symbols, AssetType type, String targetCurrency) {
        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, MarketPriceResponse> result = new LinkedHashMap<>();
        for (String sym : symbols) {
            getMarketPrice(sym, type, targetCurrency).ifPresent(p -> result.put(sym.toUpperCase(), p));
        }
        return result;
    }

    /**
     * Obtém a taxa de câmbio USD para BRL em tempo real com fallback multi-provedor
     * (Coinbase Global/US, AwesomeAPI e Binance Global) com cache de 5 minutos.
     */
    @Cacheable(value = CacheConfig.CACHE_EXCHANGE_RATES, key = "'USD_BRL'")
    public ExchangeRateResponse getUsdToBrlRate() {
        // 1. Tenta Coinbase (Sediada nos EUA, 100% acessível em qualquer VPS americana sem restrição de IP)
        Optional<BigDecimal> coinbaseRate = fetchCoinbaseUsdToBrl();
        if (coinbaseRate.isPresent()) {
            return ExchangeRateResponse.builder()
                    .from("USD")
                    .to("BRL")
                    .rate(coinbaseRate.get().setScale(4, RoundingMode.HALF_UP))
                    .source("COINBASE_FX")
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        // 2. Tenta AwesomeAPI (Especialista em câmbio BRL com dados comerciais oficiais e sem bloqueio de IP)
        Optional<BigDecimal> awesomeRate = fetchAwesomeApiUsdToBrl();
        if (awesomeRate.isPresent()) {
            return ExchangeRateResponse.builder()
                    .from("USD")
                    .to("BRL")
                    .rate(awesomeRate.get().setScale(4, RoundingMode.HALF_UP))
                    .source("AWESOME_API")
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        // 3. Tenta Binance Global (par USDT/BRL caso a VPS esteja fora dos EUA)
        Optional<BigDecimal> binanceRate = binanceMarketDataAdapter.fetchUsdtBrlRate();
        if (binanceRate.isPresent()) {
            return ExchangeRateResponse.builder()
                    .from("USD")
                    .to("BRL")
                    .rate(binanceRate.get().setScale(4, RoundingMode.HALF_UP))
                    .source("BINANCE_USDTBRL")
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        // 4. Fallback estático em caso de indisponibilidade externa total
        return ExchangeRateResponse.builder()
                .from("USD")
                .to("BRL")
                .rate(BigDecimal.valueOf(5.6500))
                .source("STATIC_FALLBACK")
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Optional<BigDecimal> fetchCoinbaseUsdToBrl() {
        try {
            com.fasterxml.jackson.databind.JsonNode response = httpClient.get()
                    .uri("https://api.coinbase.com/v2/exchange-rates?currency=USD")
                    .retrieve()
                    .body(com.fasterxml.jackson.databind.JsonNode.class);

            if (response != null && response.has("data") && response.get("data").has("rates")) {
                com.fasterxml.jackson.databind.JsonNode rates = response.get("data").get("rates");
                if (rates.has("BRL")) {
                    BigDecimal rate = new BigDecimal(rates.get("BRL").asText()).setScale(4, RoundingMode.HALF_UP);
                    log.debug("Taxa USD/BRL obtida via Coinbase FX: {}", rate);
                    return Optional.of(rate);
                }
            }
        } catch (Exception e) {
            log.debug("Coinbase FX indisponível: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> fetchAwesomeApiUsdToBrl() {
        try {
            com.fasterxml.jackson.databind.JsonNode response = httpClient.get()
                    .uri("https://economia.awesomeapi.com.br/last/USD-BRL")
                    .retrieve()
                    .body(com.fasterxml.jackson.databind.JsonNode.class);

            if (response != null && response.has("USDBRL") && response.get("USDBRL").has("bid")) {
                BigDecimal rate = new BigDecimal(response.get("USDBRL").get("bid").asText()).setScale(4, RoundingMode.HALF_UP);
                log.debug("Taxa USD/BRL obtida via AwesomeAPI: {}", rate);
                return Optional.of(rate);
            }
        } catch (Exception e) {
            log.debug("AwesomeAPI indisponível: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Busca ativos por texto/símbolo tanto na base local do Firestore quanto em catálogo comum.
     */
    @Cacheable(value = CacheConfig.CACHE_ASSET_SEARCH, key = "#query.toLowerCase()")
    public List<AssetSearchResultResponse> searchAssets(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        String q = query.trim().toUpperCase();
        List<AssetSearchResultResponse> results = new ArrayList<>();

        // 1. Busca nos ativos cadastrados no Firebase Firestore
        try {
            List<AssetDocument> localAssets = assetFireBaseAdapter.findAll();
            for (AssetDocument a : localAssets) {
                if ((a.getSymbol() != null && a.getSymbol().toUpperCase().contains(q)) ||
                    (a.getName() != null && a.getName().toUpperCase().contains(q))) {

                    AssetType type = AssetType.fromString(a.getType());
                    Optional<MarketPriceResponse> priceOpt = getMarketPrice(a.getSymbol(), type, "USD");

                    results.add(AssetSearchResultResponse.builder()
                            .symbol(a.getSymbol())
                            .name(a.getName())
                            .type(type)
                            .currentPrice(priceOpt.map(MarketPriceResponse::getPrice).orElse(null))
                            .currency("USD")
                            .imgUrl(a.getImgUrl())
                            .origin("LOCAL")
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("Erro ao buscar ativos locais: {}", e.getMessage());
        }

        // 2. Se a busca bater com criptos populares conhecidas e ainda não estiver na lista, adiciona sugestão
        List<String> popularCryptos = List.of("BTC", "ETH", "SOL", "BNB", "ADA", "DOGE", "XRP", "DOT", "AVAX", "LINK");
        for (String pop : popularCryptos) {
            if (pop.contains(q) && results.stream().noneMatch(r -> r.getSymbol().equalsIgnoreCase(pop))) {
                Optional<MarketPriceResponse> priceOpt = getMarketPrice(pop, AssetType.CRYPTO, "USD");
                if (priceOpt.isPresent()) {
                    MarketPriceResponse pr = priceOpt.get();
                    results.add(AssetSearchResultResponse.builder()
                            .symbol(pop)
                            .name(getDisplayNameForCrypto(pop))
                            .type(AssetType.CRYPTO)
                            .currentPrice(pr.getPrice())
                            .currency("USD")
                            .imgUrl(String.format("https://assets.coingecko.com/coins/images/%s/large/%s.png", pop.toLowerCase(), pop.toLowerCase()))
                            .origin("MARKET")
                            .build());
                }
            }
        }

        return results;
    }

    private void enrichWithCurrencies(MarketPriceResponse response, String targetCurrency) {
        BigDecimal usdToBrl = getUsdToBrlRate().getRate();

        if ("USD".equalsIgnoreCase(response.getCurrency())) {
            BigDecimal priceUsd = response.getPrice();
            response.setPriceInUsd(priceUsd);
            response.setPriceInBrl(priceUsd.multiply(usdToBrl).setScale(2, RoundingMode.HALF_UP));

            if ("BRL".equalsIgnoreCase(targetCurrency)) {
                response.setPrice(response.getPriceInBrl());
                response.setCurrency("BRL");
            }
        } else if ("BRL".equalsIgnoreCase(response.getCurrency())) {
            BigDecimal priceBrl = response.getPrice();
            response.setPriceInBrl(priceBrl);
            if (usdToBrl.compareTo(BigDecimal.ZERO) > 0) {
                response.setPriceInUsd(priceBrl.divide(usdToBrl, 2, RoundingMode.HALF_UP));
            }

            if ("USD".equalsIgnoreCase(targetCurrency)) {
                response.setPrice(response.getPriceInUsd());
                response.setCurrency("USD");
            }
        }
    }

    private String getDisplayNameForCrypto(String symbol) {
        switch (symbol.toUpperCase()) {
            case "BTC": return "Bitcoin";
            case "ETH": return "Ethereum";
            case "SOL": return "Solana";
            case "BNB": return "BNB";
            case "ADA": return "Cardano";
            case "DOGE": return "Dogecoin";
            case "XRP": return "XRP";
            case "DOT": return "Polkadot";
            case "AVAX": return "Avalanche";
            case "LINK": return "Chainlink";
            default: return symbol.toUpperCase();
        }
    }
}
