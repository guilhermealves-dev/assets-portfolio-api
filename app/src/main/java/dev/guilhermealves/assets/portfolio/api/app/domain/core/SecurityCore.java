package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.UserFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityCore {

    private final UserFireBaseAdapter userFireBaseAdapter;
    private final Firestore dbFirestore;

    public User getCurrentUser() throws Exception {
        Optional<User> opUser = userFireBaseAdapter.findById("8687cc99-a4e6-4fc3-9406-310f8f325b3f");
        if(opUser.isPresent()){
            return opUser.get();
        }

        return null;
    }

    public DocumentReference getCurrentUserDoc() throws Exception {
        User user = getCurrentUser();
        DocumentReference documentReference = dbFirestore.collection(UserFireBaseAdapter.COLLECTION_NAME).document(user.getId());
        return documentReference;
    }
}
