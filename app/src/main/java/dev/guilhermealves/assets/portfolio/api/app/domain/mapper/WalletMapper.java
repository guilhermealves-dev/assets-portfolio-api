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
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mappings({
        @Mapping(target = "user", expression = "java(getUser(document))"),
    })
    Wallet mapper(WalletDocument document) throws Exception;

    @Mappings({
            @Mapping(target = "userId", ignore = true),
            @Mapping(target = "user", ignore = true)
    })
    WalletDocument mapper(Wallet wallet);

    List<Wallet> mapperWalletList(List<WalletDocument> documents);

    List<WalletDocument> mapperDocList(List<Wallet> wallets);

    default User getUser(WalletDocument document) throws Exception {
        DocumentReference docRef = document.getUser();
        ApiFuture<DocumentSnapshot> query = docRef.get();
        DocumentSnapshot snapshot = query.get();

        UserDocument doc = snapshot.toObject(UserDocument.class);
        UserMapper mapper = Mappers.getMapper(UserMapper.class);

        return mapper.mapper(doc);
    }
}
