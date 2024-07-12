package dev.guilhermealves.assets.portfolio.api.app.adapters.out;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.AssetDocument;
import dev.guilhermealves.assets.portfolio.api.app.ports.out.DataBaseIntegration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class AssetFireBaseAdapter implements DataBaseIntegration<AssetDocument, String> {

    private final Firestore dbFirestore;

    public static final String COLLECTION_NAME = "assets";

    @Override
    public AssetDocument create(AssetDocument asset) throws Exception {
        try{
            asset.setId(UUID.randomUUID().toString());

            ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME)
                    .document(asset.getId())
                    .set(asset);

            log.info("Asset created on - {}", collectionApiFuture.get().getUpdateTime().toString());

            return asset;

        } catch (Throwable t){
            log.error("Error save - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public AssetDocument update(AssetDocument asset) throws Exception {
        try{
            ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME).document(asset.getId())
                    .set(asset);

            log.info("Asset updated on - {}", collectionApiFuture.get().getUpdateTime().toString());

            return asset;

        } catch (Throwable t){
            log.error("Error update - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public Optional<AssetDocument> findById(String id) throws Exception {
        try {
            DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document(id);

            ApiFuture<DocumentSnapshot> future = documentReference.get();

            DocumentSnapshot document = future.get();

            if (document.exists()) {
                AssetDocument assetDoc = document.toObject(AssetDocument.class);

                return Optional.of(assetDoc);
            }

            return Optional.empty();

        } catch (Throwable t) {
            log.error("Error find by Id - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public List<AssetDocument> findAll() throws Exception {
        try {
            Iterable<DocumentReference> documentReference = dbFirestore.collection(COLLECTION_NAME).listDocuments();
            Iterator<DocumentReference> iterator = documentReference.iterator();

            List<AssetDocument> assetList = new ArrayList<>();

            while (iterator.hasNext()) {
                DocumentReference documentRef = iterator.next();
                ApiFuture<DocumentSnapshot> future = documentRef.get();
                DocumentSnapshot document = future.get();

                AssetDocument assetDoc = document.toObject(AssetDocument.class);

                assetList.add(assetDoc);
            }
            return assetList;

        } catch (Throwable t) {
            log.error("Error find all - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public void deleteById(String id) throws Exception {
        try {
            ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME).document(id).delete();

            log.info("Asset with ID {} has been deleted successfully - {}", id, collectionApiFuture.get().getUpdateTime().toString());

        } catch (Throwable t) {
            log.error("Error delete By Id - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public DocumentReference getDocReference(String id) {
        return dbFirestore.collection(COLLECTION_NAME).document(id);
    }
}
