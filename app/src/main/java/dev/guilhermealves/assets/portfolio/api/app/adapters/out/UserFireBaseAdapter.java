package dev.guilhermealves.assets.portfolio.api.app.adapters.out;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.UserDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.mapper.UserMapper;
import dev.guilhermealves.assets.portfolio.api.app.ports.out.DataBaseIntegration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class UserFireBaseAdapter implements DataBaseIntegration<UserDocument, String> {

    private final Firestore dbFirestore;
    private final UserMapper mapper;

    public static final String COLLECTION_NAME = "users";

    @Override
    public UserDocument create(UserDocument user) throws Exception {
        try{
            user.setId(UUID.randomUUID().toString());

            ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME)
                    .document(user.getId())
                    .set(user);

            log.info("User created on - {}", collectionApiFuture.get().getUpdateTime().toString());

            return user;

        } catch (Throwable t){
            log.error("Error save - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public UserDocument update(UserDocument user) throws Exception {
        try{
            ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME).document(user.getId())
                    .set(user);

            log.info("User updated on - {}", collectionApiFuture.get().getUpdateTime().toString());

            return user;

        } catch (Throwable t){
            log.error("Error update - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public Optional<UserDocument> findById(String id) throws Exception {
        try {
            DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document(id);

            ApiFuture<DocumentSnapshot> future = documentReference.get();

            DocumentSnapshot document = future.get();

            if (document.exists()) {
                UserDocument userDoc = document.toObject(UserDocument.class);

                return Optional.of(userDoc);
            }

            return Optional.empty();

        } catch (Throwable t) {
            log.error("Error find by Id - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public List<UserDocument> findAll() throws Exception {
        try {
            Iterable<DocumentReference> documentReference = dbFirestore.collection(COLLECTION_NAME).listDocuments();
            Iterator<DocumentReference> iterator = documentReference.iterator();

            List<UserDocument> userList = new ArrayList<>();

            while (iterator.hasNext()) {
                DocumentReference documentRef = iterator.next();
                ApiFuture<DocumentSnapshot> future = documentRef.get();
                DocumentSnapshot document = future.get();

                UserDocument userDoc = document.toObject(UserDocument.class);

                userList.add(userDoc);
            }
            return userList;

        } catch (Throwable t) {
            log.error("Error find all - {}", t.getMessage());
            throw t;
        }
    }

    @Override
    public void deleteById(String id) throws Exception {
        try {
            ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME).document(id).delete();

            log.info("User with ID {} has been deleted successfully - {}", id, collectionApiFuture.get().getUpdateTime().toString());

        } catch (Throwable t) {
            log.error("Error delete By Id - {}", t.getMessage());
            throw t;
        }
    }
}
