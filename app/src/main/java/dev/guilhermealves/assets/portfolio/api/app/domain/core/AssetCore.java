package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import dev.guilhermealves.assets.portfolio.api.app.adapters.out.AssetFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.AssetDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.mapper.AssetMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.AssetRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.AssetResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.AssetSearchResultResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.MarketPriceResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import dev.guilhermealves.assets.portfolio.api.app.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetCore {

    private final AssetFireBaseAdapter assetFireBaseAdapter;
    private final AssetMapper assetMapper;
    private final MarketDataService marketDataService;
    private final AssetIconResolver assetIconResolver;

    public AssetResponse create(AssetRequest asset) throws Exception {
        if (asset.getImgUrl() == null || !assetIconResolver.isValidIconUrl(asset.getImgUrl())) {
            asset.setImgUrl(assetIconResolver.resolveIconUrl(asset.getSymbol(), asset.getType()));
        }
        AssetDocument docRequest = assetMapper.map(asset);
        AssetDocument doc = assetFireBaseAdapter.create(docRequest);
        return assetMapper.map(doc);
    }

    public AssetResponse update(AssetRequest asset) throws Exception {
        assetFireBaseAdapter.findById(asset.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ativo", asset.getId()));

        AssetDocument docRequest = assetMapper.map(asset);
        AssetDocument doc = assetFireBaseAdapter.update(docRequest);
        return assetMapper.map(doc);
    }

    public List<AssetResponse> findAll() throws Exception {
        List<AssetDocument> assetDocs = assetFireBaseAdapter.findAll();
        return assetMapper.mapList(assetDocs);
    }

    public Optional<AssetResponse> findById(String id) throws Exception {
        Optional<AssetDocument> optionalDoc = assetFireBaseAdapter.findById(id);
        if (optionalDoc.isPresent()) {
            return Optional.of(assetMapper.map(optionalDoc.get()));
        }
        return Optional.empty();
    }

    public Optional<AssetResponse> findBySymbol(String symbol) throws Exception {
        Optional<AssetDocument> optionalDoc = assetFireBaseAdapter.findBySymbol(symbol);
        return optionalDoc.map(assetMapper::map);
    }

    public Optional<AssetResponse> findByIdOrSymbol(String identifier) throws Exception {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }
        Optional<AssetResponse> byId = findById(identifier);
        if (byId.isPresent()) {
            return byId;
        }
        return findBySymbol(identifier);
    }

    public AssetDocument findOrCreateAsset(String identifier, AssetType hintType, String hintName) throws Exception {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("O identificador ou símbolo do ativo não pode ser vazio");
        }

        String trimmed = identifier.trim();

        // 1. Tenta buscar pelo ID direto no Firestore
        Optional<AssetDocument> byIdOpt = assetFireBaseAdapter.findById(trimmed);
        if (byIdOpt.isPresent()) {
            return byIdOpt.get();
        }

        // 2. Tenta buscar pelo símbolo no Firestore (ex: NVDA, BTC, PETR4)
        Optional<AssetDocument> bySymbolOpt = assetFireBaseAdapter.findBySymbol(trimmed);
        if (bySymbolOpt.isPresent()) {
            AssetDocument existing = bySymbolOpt.get();
            // Se o usuário especificou explicitamente um tipo diferente do cadastrado (ex: foi criado como ETF mas é CRYPTO), corrige
            if (hintType != null && !hintType.name().equalsIgnoreCase(existing.getType())) {
                existing.setType(hintType.name());
                String correctLogo = assetIconResolver.resolveIconUrl(existing.getSymbol(), hintType);
                if (correctLogo != null) {
                    existing.setImgUrl(correctLogo);
                }
                assetFireBaseAdapter.update(existing);
                log.info("Corrigido tipo do ativo '{}' de {} para {}", existing.getSymbol(), existing.getType(), hintType);
            }
            return existing;
        }

        // 3. O ativo não existe no Firestore: buscar metadados de mercado e auto-provisionar
        String symbol = trimmed.toUpperCase();
        String name = (hintName != null && !hintName.isBlank()) ? hintName : symbol;
        AssetType type = hintType != null ? hintType : AssetType.OTHER;
        String imgUrl = null;

        // Se o tipo foi explicitamente fornecido (ex: CRYPTO), busca cotação diretamente com esse tipo para evitar conflitos
        if (hintType != null && hintType != AssetType.OTHER) {
            try {
                Optional<MarketPriceResponse> priceOpt = marketDataService.getMarketPrice(symbol, hintType, "USD");
                if (priceOpt.isPresent()) {
                    MarketPriceResponse pr = priceOpt.get();
                    if (pr.getName() != null && !pr.getName().isBlank() && (hintName == null || hintName.isBlank())) {
                        name = pr.getName();
                    }
                    type = hintType;
                }
            } catch (Exception e) {
                log.warn("Erro ao buscar cotação direta para {} com tipo {}: {}", symbol, hintType, e.getMessage());
            }
        } else {
            // 3.1. Tenta resolver via MarketDataService.searchAssets
            try {
                List<AssetSearchResultResponse> searchResults = marketDataService.searchAssets(trimmed);
                Optional<AssetSearchResultResponse> matchOpt = searchResults.stream()
                        .filter(r -> r.getSymbol() != null && r.getSymbol().equalsIgnoreCase(trimmed))
                        .findFirst();

                if (matchOpt.isPresent()) {
                    AssetSearchResultResponse match = matchOpt.get();
                    symbol = match.getSymbol().toUpperCase();
                    if (match.getName() != null && !match.getName().isBlank() && (hintName == null || hintName.isBlank())) {
                        name = match.getName();
                    }
                    if (match.getType() != null) {
                        type = match.getType();
                    }
                    if (match.getImgUrl() != null && !match.getImgUrl().isBlank()) {
                        imgUrl = match.getImgUrl();
                    }
                }
            } catch (Exception e) {
                log.warn("Erro ao buscar metadados em searchAssets para {}: {}", trimmed, e.getMessage());
            }
        }

        // 3.2. Se o tipo ainda for OTHER ou nome for o próprio ticker, tenta getMarketPrice
        if (type == AssetType.OTHER || name.equals(symbol)) {
            try {
                Optional<MarketPriceResponse> priceOpt = marketDataService.getMarketPrice(symbol, hintType, "USD");
                if (priceOpt.isPresent()) {
                    MarketPriceResponse price = priceOpt.get();
                    if (price.getName() != null && !price.getName().isBlank()) {
                        name = price.getName();
                    }
                    if (price.getType() != null && hintType == null) {
                        type = price.getType();
                    }
                }
            } catch (Exception e) {
                log.warn("Erro ao buscar cotação de mercado para {}: {}", trimmed, e.getMessage());
            }
        }

        // 3.3. Resolução robusta de ícone/logo com fallback de CDN oficial
        if (imgUrl == null || !assetIconResolver.isValidIconUrl(imgUrl)) {
            imgUrl = assetIconResolver.resolveIconUrl(symbol, type);
        }

        // 4. Cria e persiste o novo AssetDocument no Firestore
        AssetDocument newAsset = new AssetDocument();
        newAsset.setSymbol(symbol);
        newAsset.setName(name);
        newAsset.setType(type.name());
        newAsset.setImgUrl(imgUrl);

        log.info("Auto-provisionando ativo '{}' ({}) do tipo {} no Firestore", symbol, name, type);
        return assetFireBaseAdapter.create(newAsset);
    }

    public void deleteById(String id) throws Exception {
        assetFireBaseAdapter.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ativo", id));

        assetFireBaseAdapter.deleteById(id);
    }
}
