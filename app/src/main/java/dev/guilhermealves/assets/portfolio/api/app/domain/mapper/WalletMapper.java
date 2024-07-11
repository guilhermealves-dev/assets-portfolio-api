package dev.guilhermealves.assets.portfolio.api.app.domain.mapper;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.UserDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.WalletDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.User;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.concurrent.ExecutionException;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mappings({
        @Mapping(target = "user", expression = "java(getUser(document))")
    })
    Wallet mapper(WalletDocument document) throws ExecutionException, InterruptedException;

    WalletDocument mapper(Wallet wallet);

    default User getUser(WalletDocument document) throws ExecutionException, InterruptedException {
        DocumentReference userDocRef = document.getUser_id();
        ApiFuture<DocumentSnapshot> userQuery = userDocRef.get();
        DocumentSnapshot userSnap = userQuery.get();

        UserDocument userDoc = userSnap.toObject(UserDocument.class);


        return User.builder()
                .id(userDoc.getId())
                .name(userDoc.getName())
                .email(userDoc.getEmail())
                .password(userDoc.getPassword())
                .build();
    }
}
