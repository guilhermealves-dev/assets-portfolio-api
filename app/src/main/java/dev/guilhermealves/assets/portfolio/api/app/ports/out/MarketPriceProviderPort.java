package dev.guilhermealves.assets.portfolio.api.app.ports.out;

import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.MarketPriceResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;

import java.util.Optional;

public interface MarketPriceProviderPort {

    /**
     * Obtém a cotação de mercado atual de um ativo.
     * @param symbol Símbolo / Ticker do ativo (ex: "BTC", "ETH", "PETR4", "AAPL")
     * @return MarketPriceResponse com preço e dados em tempo real
     */
    Optional<MarketPriceResponse> getPrice(String symbol);

    /**
     * Verifica se o provedor suporta a classe de ativo informada.
     */
    boolean supports(AssetType type);

    /**
     * Nome identificador do provedor (ex: "BINANCE", "COINBASE", "BYBIT", "BRAPI", "YAHOO").
     */
    String getProviderName();

    /**
     * Prioridade na cadeia de execução (menor valor = maior prioridade).
     */
    default int getPriority() {
        return 10;
    }
}
