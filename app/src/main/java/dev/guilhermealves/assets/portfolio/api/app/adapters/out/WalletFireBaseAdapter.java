package dev.guilhermealves.assets.portfolio.api.app.adapters.out;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.UserDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.mapper.WalletMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.Wallet;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import dev.guilhermealves.assets.portfolio.api.app.domain.entity.WalletDocument;
import dev.guilhermealves.assets.portfolio.api.app.ports.out.DataBaseIntegration;

@Service
@Slf4j
@AllArgsConstructor
public class WalletFireBaseAdapter implements DataBaseIntegration<Wallet, String> {

    private final Firestore dbFirestore;
    private final WalletMapper mapper;

    private static final String COLLECTION_NAME = "wallets";

    @Override
    public Wallet save(Wallet t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public Optional<Wallet> findById(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }

    @Override
    public List<Wallet> findAll() throws ExecutionException, InterruptedException {
        try {
            Iterable<DocumentReference> documentReference = dbFirestore.collection(COLLECTION_NAME).listDocuments();
            Iterator<DocumentReference> iterator = documentReference.iterator();

            List<Wallet> walletList = new ArrayList<>();

            while (iterator.hasNext()) {
                DocumentReference documentRef = iterator.next();
                ApiFuture<DocumentSnapshot> future = documentRef.get();
                DocumentSnapshot document = future.get();

                WalletDocument walletDoc = document.toObject(WalletDocument.class);
                Wallet wallet = mapper.mapper(walletDoc);

                walletList.add(wallet);
            }
            return walletList;
        } catch (Throwable t) {
            log.error("Error find all - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public void deleteById(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteById'");
    }

    private UserDocument getUser(DocumentSnapshot document) throws ExecutionException, InterruptedException {
        DocumentReference userDocRef = (DocumentReference) document.get("user");
        ApiFuture<DocumentSnapshot> userQuery = userDocRef.get();
        DocumentSnapshot userDoc = userQuery.get();
        UserDocument user = userDoc.toObject(UserDocument.class);

        return user;
    }

}
