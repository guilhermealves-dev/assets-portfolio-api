package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.UserFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.UserDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.mapper.UserMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityCore {

    private final Firestore dbFirestore;
    private final UserMapper userMapper;
    private final UserFireBaseAdapter userFireBaseAdapter;

    public UserResponse getCurrentUser() throws Exception {
        //TODO: search for active user
        Optional<UserDocument> opUser = userFireBaseAdapter.findById("8687cc99-a4e6-4fc3-9406-310f8f325b3f");
        if(opUser.isPresent()){
            return userMapper.map(opUser.get());
        }

        return null;
    }

    public DocumentReference getCurrentUserDoc() throws Exception {
        UserResponse user = getCurrentUser();
        DocumentReference documentReference = dbFirestore.collection(UserFireBaseAdapter.COLLECTION_NAME).document(user.getId());
        return documentReference;
    }
}
