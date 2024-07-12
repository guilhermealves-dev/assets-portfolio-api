package dev.guilhermealves.assets.portfolio.api.app.domain.mapper;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.AssetDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.TransactionDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.WalletDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.Asset;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.Transaction;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mappings({
            @Mapping(target = "asset", expression = "java(getAsset(document))"),
            @Mapping(target = "wallet", expression = "java(getWallet(document))"),
    })
    Transaction mapper(TransactionDocument document) throws Exception;

    @Mappings({
            @Mapping(target = "assetId", ignore = true),
            @Mapping(target = "walletId", ignore = true)
    })
    TransactionDocument mapper(Transaction transaction);

    List<Transaction> mapperTransactionList(List<TransactionDocument> documents);

    List<TransactionDocument> mapperDocList(List<Transaction> transactions);

    default Asset getAsset(TransactionDocument document) throws Exception {
        DocumentReference docRef = document.getAssetId();
        ApiFuture<DocumentSnapshot> query = docRef.get();
        DocumentSnapshot snapshot = query.get();

        AssetDocument doc = snapshot.toObject(AssetDocument.class);
        AssetMapper mapper = Mappers.getMapper(AssetMapper.class);

        return mapper.mapper(doc);
    }

    default Wallet getWallet(TransactionDocument document) throws Exception {
        DocumentReference docRef = document.getWalletId();
        ApiFuture<DocumentSnapshot> query = docRef.get();
        DocumentSnapshot snapshot = query.get();

        WalletDocument doc = snapshot.toObject(WalletDocument.class);
        WalletMapper mapper = Mappers.getMapper(WalletMapper.class);

        return mapper.mapper(doc);
    }
}
