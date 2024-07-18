package dev.guilhermealves.assets.portfolio.api.app.domain.mapper;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.UserDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.WalletDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.WalletRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.UserResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.WalletResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "user", ignore = true)
    })
    WalletDocument map(WalletRequest request);

    @Mappings({
        @Mapping(target = "user", expression = "java(getUser(document))"),
    })
    WalletResponse map(WalletDocument document) throws Exception;

    List<WalletResponse> mapList(List<WalletDocument> documents);

    default UserResponse getUser(WalletDocument document) throws Exception {
        DocumentReference docRef = document.getUser();
        ApiFuture<DocumentSnapshot> query = docRef.get();
        DocumentSnapshot snapshot = query.get();

        UserDocument doc = snapshot.toObject(UserDocument.class);
        UserMapper userMapper = Mappers.getMapper(UserMapper.class);

        return userMapper.map(doc);
    }
}
