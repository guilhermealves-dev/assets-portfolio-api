package dev.guilhermealves.assets.portfolio.api.app.adapters.in;

import dev.guilhermealves.assets.portfolio.api.app.domain.core.MarketDataService;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.AssetSearchResultResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.ExchangeRateResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.MarketPriceResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import dev.guilhermealves.assets.portfolio.api.app.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
@Tag(name = "Market Data", description = "Endpoints de cotações em tempo real de exchanges públicas (Binance, Coinbase, Bybit), ações e câmbio")
public class MarketControllerAdapter {

    private final MarketDataService marketDataService;

    @Operation(summary = "Obtém a cotação em tempo real de um ativo específico")
    @GetMapping("/prices/{symbol}")
    public ResponseEntity<MarketPriceResponse> getPrice(
            @Parameter(description = "Ticker ou símbolo do ativo (ex: BTC, ETH, PETR4, AAPL)")
            @PathVariable String symbol,
            @Parameter(description = "Classe do ativo (padrão: CRYPTO)")
            @RequestParam(required = false, defaultValue = "CRYPTO") AssetType type,
            @Parameter(description = "Moeda de destino (USD ou BRL, padrão: USD)")
            @RequestParam(required = false, defaultValue = "USD") String currency) {

        MarketPriceResponse price = marketDataService.getMarketPrice(symbol, type, currency)
                .orElseThrow(() -> new ResourceNotFoundException("Cotação não encontrada para o ativo", symbol));

        return ResponseEntity.ok(price);
    }

    @Operation(summary = "Obtém as cotações em tempo real de múltiplos ativos")
    @GetMapping("/prices")
    public ResponseEntity<Map<String, MarketPriceResponse>> getMultiplePrices(
            @Parameter(description = "Lista de símbolos separados por vírgula (ex: BTC,ETH,SOL)")
            @RequestParam List<String> symbols,
            @Parameter(description = "Classe dos ativos (padrão: CRYPTO)")
            @RequestParam(required = false, defaultValue = "CRYPTO") AssetType type,
            @Parameter(description = "Moeda de destino (USD ou BRL, padrão: USD)")
            @RequestParam(required = false, defaultValue = "USD") String currency) {

        Map<String, MarketPriceResponse> prices = marketDataService.getMultiplePrices(symbols, type, currency);
        return ResponseEntity.ok(prices);
    }

    @Operation(summary = "Obtém a taxa de câmbio atual (ex: USD para BRL)")
    @GetMapping("/exchange-rate")
    public ResponseEntity<ExchangeRateResponse> getExchangeRate(
            @RequestParam(required = false, defaultValue = "USD") String from,
            @RequestParam(required = false, defaultValue = "BRL") String to) {

        ExchangeRateResponse rate = marketDataService.getUsdToBrlRate();
        return ResponseEntity.ok(rate);
    }

    @Operation(summary = "Busca ativos por nome ou símbolo com preços e ícones")
    @GetMapping("/search")
    public ResponseEntity<List<AssetSearchResultResponse>> search(
            @Parameter(description = "Termo de busca (ex: btc, petr, apple)")
            @RequestParam String query) {

        List<AssetSearchResultResponse> results = marketDataService.searchAssets(query);
        return ResponseEntity.ok(results);
    }
}
