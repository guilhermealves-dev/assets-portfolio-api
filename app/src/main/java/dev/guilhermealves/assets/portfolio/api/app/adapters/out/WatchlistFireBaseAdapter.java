package dev.guilhermealves.assets.portfolio.api.app.adapters.out;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.WatchlistDocument;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class WatchlistFireBaseAdapter {

    private final Firestore dbFirestore;

    public static final String COLLECTION_NAME = "watchlists";

    public WatchlistDocument create(WatchlistDocument doc) throws Exception {
        if (doc.getId() == null || doc.getId().isBlank()) {
            doc.setId(UUID.randomUUID().toString());
        }
        if (doc.getCreatedAt() == null) {
            doc.setCreatedAt(Timestamp.now());
        }

        ApiFuture<WriteResult> future = dbFirestore.collection(COLLECTION_NAME)
                .document(doc.getId())
                .set(doc);

        log.info("Watchlist item created: {} for user: {} - update time: {}", doc.getId(), doc.getUserId(), future.get().getUpdateTime());
        return doc;
    }

    public Optional<WatchlistDocument> findById(String id) throws Exception {
        DocumentSnapshot snapshot = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();
        if (snapshot.exists()) {
            WatchlistDocument doc = snapshot.toObject(WatchlistDocument.class);
            if (doc != null) {
                doc.setId(snapshot.getId());
                return Optional.of(doc);
            }
        }
        return Optional.empty();
    }

    public List<WatchlistDocument> findByUserId(String userId) throws Exception {
        if (userId == null || userId.isBlank()) {
            return Collections.emptyList();
        }

        QuerySnapshot querySnapshot = dbFirestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get()
                .get();

        return querySnapshot.getDocuments().stream()
                .map(doc -> {
                    WatchlistDocument w = doc.toObject(WatchlistDocument.class);
                    if (w != null) {
                        w.setId(doc.getId());
                    }
                    return w;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Optional<WatchlistDocument> findByUserIdAndAsset(String userId, String assetId, String symbol) throws Exception {
        List<WatchlistDocument> userItems = findByUserId(userId);
        return userItems.stream()
                .filter(item -> (assetId != null && assetId.equalsIgnoreCase(item.getAssetId()))
                        || (symbol != null && symbol.equalsIgnoreCase(item.getAssetSymbol())))
                .findFirst();
    }

    public void deleteById(String id) throws Exception {
        dbFirestore.collection(COLLECTION_NAME).document(id).delete().get();
        log.info("Watchlist item deleted: {}", id);
    }

    public boolean deleteByUserIdAndAsset(String userId, String assetIdOrSymbol) throws Exception {
        List<WatchlistDocument> userItems = findByUserId(userId);
        Optional<WatchlistDocument> match = userItems.stream()
                .filter(item -> (item.getAssetId() != null && item.getAssetId().equalsIgnoreCase(assetIdOrSymbol))
                        || (item.getAssetSymbol() != null && item.getAssetSymbol().equalsIgnoreCase(assetIdOrSymbol))
                        || (item.getId() != null && item.getId().equalsIgnoreCase(assetIdOrSymbol)))
                .findFirst();

        if (match.isPresent()) {
            deleteById(match.get().getId());
            return true;
        }
        return false;
    }
}
