package dev.guilhermealves.assets.portfolio.api.domain.core;

import dev.guilhermealves.assets.portfolio.api.app.domain.core.AssetIconResolver;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssetIconResolverTest {

    private AssetIconResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new AssetIconResolver();
    }

    @Test
    @DisplayName("Deve resolver ícone de criptomoeda via CoinCap CDN")
    void deveResolverIconeCripto() {
        String urlBtc = resolver.resolveIconUrl("BTC", AssetType.CRYPTO);
        assertEquals("https://assets.coincap.io/assets/icons/btc@2x.png", urlBtc);

        String urlEth = resolver.resolveIconUrl("ETH", AssetType.CRYPTO);
        assertEquals("https://assets.coincap.io/assets/icons/eth@2x.png", urlEth);
    }

    @Test
    @DisplayName("Deve resolver ícone de ações americanas e ETFs via Financial Modeling Prep")
    void deveResolverIconeAcaoUS() {
        String urlNvda = resolver.resolveIconUrl("NVDA", AssetType.STOCK_US);
        assertEquals("https://financialmodelingprep.com/image-stock/NVDA.png", urlNvda);

        String urlSpy = resolver.resolveIconUrl("SPY", AssetType.ETF);
        assertEquals("https://financialmodelingprep.com/image-stock/SPY.png", urlSpy);
    }

    @Test
    @DisplayName("Deve resolver ícone de ações brasileiras e FIIs via Brapi Icons")
    void deveResolverIconeAcaoBR() {
        String urlPetr = resolver.resolveIconUrl("PETR4", AssetType.STOCK_BR);
        assertEquals("https://icons.brapi.dev/icons/PETR4.svg", urlPetr);

        String urlHglg = resolver.resolveIconUrl("HGLG11", AssetType.FII);
        assertEquals("https://icons.brapi.dev/icons/HGLG11.svg", urlHglg);
    }

    @Test
    @DisplayName("Deve identificar URLs inválidas do CoinGecko construídas com ticker")
    void deveIdentificarUrlsInvalidas() {
        assertFalse(resolver.isValidIconUrl(null));
        assertFalse(resolver.isValidIconUrl(""));
        assertFalse(resolver.isValidIconUrl("https://assets.coingecko.com/coins/images/btc/large/btc.png"));
        assertFalse(resolver.isValidIconUrl("https://assets.coingecko.com/coins/images/eth/large/eth.png"));

        assertTrue(resolver.isValidIconUrl("https://assets.coincap.io/assets/icons/btc@2x.png"));
        assertTrue(resolver.isValidIconUrl("https://financialmodelingprep.com/image-stock/NVDA.png"));
        assertTrue(resolver.isValidIconUrl("https://icons.brapi.dev/icons/PETR4.svg"));
    }
}
