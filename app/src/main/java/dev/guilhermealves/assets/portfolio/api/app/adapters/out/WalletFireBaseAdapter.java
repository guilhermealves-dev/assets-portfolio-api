package dev.guilhermealves.assets.portfolio.api.app.adapters.out;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.WalletDocument;
import dev.guilhermealves.assets.portfolio.api.app.ports.out.DataBaseIntegration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class WalletFireBaseAdapter implements DataBaseIntegration<WalletDocument, String> {

    private final Firestore dbFirestore;

    private static final String COLLECTION_NAME = "wallets";

    @Override
    public WalletDocument create(WalletDocument wallet) throws Exception {
        try{
            wallet.setId(UUID.randomUUID().toString());

            ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME)
                    .document(wallet.getId())
                    .set(wallet);

            log.info("Wallet created on - {}", collectionApiFuture.get().getUpdateTime().toString());

            return wallet;

        } catch (Throwable t){
            log.error("Error save - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public WalletDocument update(WalletDocument wallet) throws Exception {
        try{
            ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME).document(wallet.getId())
                    .set(wallet);

            log.info("Wallet updated on - {}", collectionApiFuture.get().getUpdateTime().toString());

            return wallet;

        } catch (Throwable t){
            log.error("Error update - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public Optional<WalletDocument> findById(String id) throws Exception {
        try {
            DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = documentReference.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                WalletDocument walletDoc = document.toObject(WalletDocument.class);

                return Optional.of(walletDoc);
            }

            return Optional.empty();

        } catch (Throwable t){
            log.error("Error find By Id - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public List<WalletDocument> findAll() throws Exception {
        try {
            Iterable<DocumentReference> documentReference = dbFirestore.collection(COLLECTION_NAME).listDocuments();
            Iterator<DocumentReference> iterator = documentReference.iterator();

            List<WalletDocument> walletList = new ArrayList<>();

            while (iterator.hasNext()) {
                DocumentReference documentRef = iterator.next();
                ApiFuture<DocumentSnapshot> future = documentRef.get();
                DocumentSnapshot document = future.get();

                WalletDocument walletDoc = document.toObject(WalletDocument.class);

                walletList.add(walletDoc);
            }
            return walletList;

        } catch (Throwable t) {
            log.error("Error find all - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public void deleteById(String id) throws Exception {
        try {
            ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME).document(id).delete();

            log.info("Wallet with ID {} has been deleted successfully - {}", id, collectionApiFuture.get().getUpdateTime().toString());

        } catch (Throwable t) {
            log.error("Error delete By Id - {}", t.getMessage());
            throw t;
        }
    }
}
