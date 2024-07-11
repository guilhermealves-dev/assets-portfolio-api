package dev.guilhermealves.assets.portfolio.api.app.adapters.out;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.UserDocument;
import dev.guilhermealves.assets.portfolio.api.app.ports.out.DataBaseIntegration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@AllArgsConstructor
public class UserFireBaseAdapter implements DataBaseIntegration<UserDocument, String> {

    private final Firestore dbFirestore;
    private static final String COLLECTION_NAME = "users";

    @Override
    public UserDocument save(UserDocument user) {
        return null;
    }

    @Override
    public Optional<UserDocument> findById(String id) throws ExecutionException, InterruptedException {
        try {
            DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document(id);

            ApiFuture<DocumentSnapshot> future = documentReference.get();

            DocumentSnapshot document = future.get();

            if (document.exists()) {
                UserDocument user = document.toObject(UserDocument.class);
                return Optional.of(user);
            } else {
                return Optional.empty();
            }
        } catch (Throwable t) {
            log.error("Error find by Id - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public List<UserDocument> findAll() throws ExecutionException, InterruptedException {
        return List.of();
    }

    @Override
    public void deleteById(String s) {

    }
}
