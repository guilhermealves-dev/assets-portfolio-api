package dev.guilhermealves.assets.portfolio.api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_MARKET_PRICES = "market-prices";
    public static final String CACHE_EXCHANGE_RATES = "exchange-rates";
    public static final String CACHE_ASSET_SEARCH = "asset-search";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                CACHE_MARKET_PRICES,
                CACHE_EXCHANGE_RATES,
                CACHE_ASSET_SEARCH
        );

        // Configuração padrão com Caffeine (expira em 30 segundos, máx 1000 entradas)
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .maximumSize(1000)
                .recordStats());

        return cacheManager;
    }
}
