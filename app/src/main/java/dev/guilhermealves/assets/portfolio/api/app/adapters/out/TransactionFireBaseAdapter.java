package dev.guilhermealves.assets.portfolio.api.app.adapters.out;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.TransactionDocument;
import dev.guilhermealves.assets.portfolio.api.app.ports.out.DataBaseIntegration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class TransactionFireBaseAdapter implements DataBaseIntegration<TransactionDocument, String> {

    private final Firestore dbFirestore;

    public static final String COLLECTION_NAME = "transactions";

    @Override
    public TransactionDocument create(TransactionDocument transaction) throws Exception {
        try {
            if (transaction.getId() == null || transaction.getId().isBlank()) {
                transaction.setId(UUID.randomUUID().toString());
            }

            ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME)
                    .document(transaction.getId())
                    .set(transaction);

            log.info("Transaction created on - {}", collectionApiFuture.get().getUpdateTime().toString());

            return transaction;

        } catch (Throwable t) {
            log.error("Error save - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public TransactionDocument update(TransactionDocument transaction) throws Exception {
        try {
            ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME).document(transaction.getId())
                    .set(transaction);

            log.info("Transaction updated on - {}", collectionApiFuture.get().getUpdateTime().toString());

            return transaction;

        } catch (Throwable t) {
            log.error("Error update - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public Optional<TransactionDocument> findById(String id) throws Exception {
        try {
            DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document(id);

            ApiFuture<DocumentSnapshot> future = documentReference.get();

            DocumentSnapshot document = future.get();

            if (document.exists()) {
                TransactionDocument transactionDoc = document.toObject(TransactionDocument.class);
                return Optional.ofNullable(transactionDoc);
            }

            return Optional.empty();

        } catch (Throwable t) {
            log.error("Error find by Id - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public List<TransactionDocument> findAll() throws Exception {
        try {
            ApiFuture<QuerySnapshot> future = dbFirestore.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            List<TransactionDocument> list = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                list.add(document.toObject(TransactionDocument.class));
            }
            return list;

        } catch (Throwable t) {
            log.error("Error find all - {}", t.getMessage());
            throw t;
        }
    }

    public List<TransactionDocument> findByWalletId(String walletId) throws Exception {
        try {
            ApiFuture<QuerySnapshot> future = dbFirestore.collection(COLLECTION_NAME)
                    .whereEqualTo("walletId", walletId)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<TransactionDocument> list = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                list.add(document.toObject(TransactionDocument.class));
            }
            return list;
        } catch (Throwable t) {
            log.error("Error find by walletId - {}", t.getMessage());
            throw t;
        }
    }

    public List<TransactionDocument> findWithFilter(String walletId, String assetId, Integer page, Integer size) throws Exception {
        try {
            Query query = dbFirestore.collection(COLLECTION_NAME);

            if (walletId != null && !walletId.isBlank()) {
                query = query.whereEqualTo("walletId", walletId);
            }

            if (assetId != null && !assetId.isBlank()) {
                query = query.whereEqualTo("assetId", assetId);
            }

            if (size != null && size > 0) {
                int pageNumber = (page != null && page >= 0) ? page : 0;
                query = query.limit(size).offset(pageNumber * size);
            }

            ApiFuture<QuerySnapshot> future = query.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            List<TransactionDocument> list = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                list.add(document.toObject(TransactionDocument.class));
            }
            return list;
        } catch (Throwable t) {
            log.error("Error find with filter - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public void deleteById(String id) throws Exception {
        try {
            ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME).document(id).delete();
            log.info("Transaction with ID {} has been deleted successfully - {}", id, collectionApiFuture.get().getUpdateTime().toString());

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
