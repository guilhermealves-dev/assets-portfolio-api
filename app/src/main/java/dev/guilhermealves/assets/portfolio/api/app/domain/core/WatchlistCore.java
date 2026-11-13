package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import com.google.cloud.Timestamp;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.AssetFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.WatchlistFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.AssetDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.WatchlistDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.WatchlistRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.MarketPriceResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.WatchlistCheckResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.WatchlistItemResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import dev.guilhermealves.assets.portfolio.api.app.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatchlistCore {

    private final WatchlistFireBaseAdapter watchlistFireBaseAdapter;
    private final AssetFireBaseAdapter assetFireBaseAdapter;
    private final AssetCore assetCore;
    private final MarketDataService marketDataService;
    private final SecurityCore securityCore;

    public WatchlistItemResponse addToWatchlist(WatchlistRequest request, String currency) throws Exception {
        String userId = securityCore.getCurrentUserId();
        String targetCurrency = (currency != null && !currency.isBlank()) ? currency.toUpperCase() : "USD";

        AssetDocument assetDoc = assetCore.findOrCreateAsset(
                request.getEffectiveAssetIdentifier(),
                request.getAssetType(),
                request.getAssetName()
        );

        Optional<WatchlistDocument> existing = watchlistFireBaseAdapter.findByUserIdAndAsset(
                userId,
                assetDoc.getId(),
                assetDoc.getSymbol()
        );

        WatchlistDocument doc;
        if (existing.isPresent()) {
            doc = existing.get();
        } else {
            doc = WatchlistDocument.builder()
                    .userId(userId)
                    .assetId(assetDoc.getId())
                    .assetSymbol(assetDoc.getSymbol())
                    .createdAt(Timestamp.now())
                    .build();
            doc = watchlistFireBaseAdapter.create(doc);
        }

        return buildItemResponse(doc, assetDoc, targetCurrency);
    }

    public List<WatchlistItemResponse> getWatchlist(String currency) throws Exception {
        String userId = securityCore.getCurrentUserId();
        String targetCurrency = (currency != null && !currency.isBlank()) ? currency.toUpperCase() : "USD";

        List<WatchlistDocument> items = watchlistFireBaseAdapter.findByUserId(userId);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        // Cache dos ativos em memória para evitar N leituras
        Map<String, AssetDocument> assetCache = new HashMap<>();
        List<AssetDocument> allAssets = assetFireBaseAdapter.findAll();
        for (AssetDocument a : allAssets) {
            assetCache.put(a.getId(), a);
            if (a.getSymbol() != null) {
                assetCache.put(a.getSymbol().toUpperCase(), a);
            }
        }

        List<WatchlistItemResponse> responses = new ArrayList<>();
        for (WatchlistDocument doc : items) {
            AssetDocument assetDoc = null;
            if (doc.getAssetId() != null) {
                assetDoc = assetCache.get(doc.getAssetId());
            }
            if (assetDoc == null && doc.getAssetSymbol() != null) {
                assetDoc = assetCache.get(doc.getAssetSymbol().toUpperCase());
            }

            responses.add(buildItemResponse(doc, assetDoc, targetCurrency));
        }

        // Ordena por data de adição decrescente (mais recentes primeiro)
        return responses.stream()
                .sorted(Comparator.comparing(
                        WatchlistItemResponse::getAddedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .collect(Collectors.toList());
    }

    public void removeFromWatchlist(String assetIdentifier) throws Exception {
        String userId = securityCore.getCurrentUserId();
        boolean deleted = watchlistFireBaseAdapter.deleteByUserIdAndAsset(userId, assetIdentifier);
        if (!deleted) {
            log.info("Ativo {} não encontrado na watchlist do usuário {}", assetIdentifier, userId);
        }
    }

    public WatchlistCheckResponse checkWatchlist(String assetIdentifier) throws Exception {
        String userId = securityCore.getCurrentUserId();
        Optional<WatchlistDocument> match = watchlistFireBaseAdapter.findByUserIdAndAsset(userId, assetIdentifier, assetIdentifier);

        String assetId = null;
        String symbol = null;
        if (match.isPresent()) {
            assetId = match.get().getAssetId();
            symbol = match.get().getAssetSymbol();
        } else {
            // Tenta obter os dados do ativo se cadastrado
            Optional<AssetDocument> assetDoc = assetFireBaseAdapter.findById(assetIdentifier);
            if (assetDoc.isPresent()) {
                assetId = assetDoc.get().getId();
                symbol = assetDoc.get().getSymbol();
            } else {
                symbol = assetIdentifier.toUpperCase();
            }
        }

        return WatchlistCheckResponse.builder()
                .assetId(assetId)
                .symbol(symbol)
                .inWatchlist(match.isPresent())
                .build();
    }

    private WatchlistItemResponse buildItemResponse(WatchlistDocument doc, AssetDocument assetDoc, String currency) {
        String symbol = doc.getAssetSymbol();
        String name = "Ativo Desconhecido";
        AssetType type = AssetType.CRYPTO;
        String imgUrl = null;
        String assetId = doc.getAssetId();

        if (assetDoc != null) {
            assetId = assetDoc.getId();
            symbol = assetDoc.getSymbol();
            name = assetDoc.getName();
            if (assetDoc.getType() != null) {
                try {
                    type = AssetType.fromString(assetDoc.getType());
                } catch (Exception ignored) {
                    type = AssetType.CRYPTO;
                }
            }
            imgUrl = assetDoc.getImgUrl();
        }

        MarketPriceResponse marketPrice = null;
        try {
            marketPrice = marketDataService.getMarketPrice(symbol, type, currency).orElse(null);
        } catch (Exception e) {
            log.warn("Erro ao obter cotação de mercado para {} na watchlist: {}", symbol, e.getMessage());
        }

        LocalDateTime addedAt = null;
        if (doc.getCreatedAt() != null) {
            addedAt = doc.getCreatedAt().toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        return WatchlistItemResponse.builder()
                .id(doc.getId())
                .assetId(assetId)
                .symbol(symbol)
                .name(name)
                .type(type)
                .imgUrl(imgUrl)
                .currentPrice(marketPrice != null ? marketPrice.getPrice() : null)
                .change24h(marketPrice != null ? marketPrice.getChange24hPercent() : null)
                .currency(currency)
                .addedAt(addedAt)
                .build();
    }
}
