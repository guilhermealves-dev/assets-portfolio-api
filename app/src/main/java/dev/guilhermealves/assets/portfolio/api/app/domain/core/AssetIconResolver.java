package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import org.springframework.stereotype.Component;

@Component
public class AssetIconResolver {

    private static final String COINCAP_ICON_URL = "https://assets.coincap.io/assets/icons/%s@2x.png";
    private static final String FMP_STOCK_ICON_URL = "https://financialmodelingprep.com/image-stock/%s.png";
    private static final String BRAPI_STOCK_ICON_URL = "https://icons.brapi.dev/icons/%s.svg";
    private static final String UI_AVATARS_URL = "https://ui-avatars.com/api/?name=%s&background=%s&color=fff&size=128&bold=true";

    /**
     * Resolve a URL de ícone/logotipo oficial do ativo com base em seu símbolo e tipo.
     */
    public String resolveIconUrl(String symbol, AssetType type) {
        if (symbol == null || symbol.isBlank()) {
            return String.format(UI_AVATARS_URL, "ASSET", "1E293B");
        }

        String cleanSymbol = symbol.trim().toUpperCase();
        AssetType assetType = type != null ? type : AssetType.OTHER;

        switch (assetType) {
            case CRYPTO:
                return String.format(COINCAP_ICON_URL, cleanSymbol.toLowerCase());

            case STOCK_US:
            case ETF:
                return String.format(FMP_STOCK_ICON_URL, cleanSymbol);

            case STOCK_BR:
            case FII:
                return String.format(BRAPI_STOCK_ICON_URL, cleanSymbol);

            case COMMODITY:
                return String.format(UI_AVATARS_URL, cleanSymbol, "D97706");

            case CASH:
                return String.format(UI_AVATARS_URL, cleanSymbol, "059669");

            default:
                return String.format(UI_AVATARS_URL, cleanSymbol, "1E293B");
        }
    }

    /**
     * Valida se uma URL de ícone existente é funcional ou se é um formato quebrado conhecido.
     */
    public boolean isValidIconUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        String lower = url.toLowerCase().trim();

        // URLs antigas do CoinGecko construídas com ticker direto retornam 403 Forbidden
        if (lower.contains("assets.coingecko.com/coins/images/") && !lower.matches(".*/coins/images/\\d+/.*")) {
            return false;
        }

        return true;
    }
}
