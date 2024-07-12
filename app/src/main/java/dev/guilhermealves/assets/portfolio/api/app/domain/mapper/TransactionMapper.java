package dev.guilhermealves.assets.portfolio.api.app.domain.mapper;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.AssetDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.TransactionDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.WalletDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.Asset;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.Transaction;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.Wallet;
import dev.guilhermealves.assets.portfolio.api.app.domain.utils.LocalDateTimeConverter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mappings({
            @Mapping(target = "asset", expression = "java(getAsset(document))"),
            @Mapping(target = "wallet", expression = "java(getWallet(document))"),
            @Mapping(target = "datetime", expression = "java(parseToLocalDateTime(document))")
    })
    Transaction mapper(TransactionDocument document) throws Exception;

    @Mappings({
            @Mapping(target = "assetId", ignore = true),
            @Mapping(target = "asset", ignore = true),
            @Mapping(target = "walletId", ignore = true),
            @Mapping(target = "wallet", ignore = true),
            @Mapping(source = "datetime", target = "date")
    })
    TransactionDocument mapper(Transaction transaction);

    List<Transaction> mapperTransactionList(List<TransactionDocument> documents);

    List<TransactionDocument> mapperDocList(List<Transaction> transactions);

    default Asset getAsset(TransactionDocument document) throws Exception {
        DocumentReference docRef = document.getAsset();
        ApiFuture<DocumentSnapshot> query = docRef.get();
        DocumentSnapshot snapshot = query.get();

        AssetDocument doc = snapshot.toObject(AssetDocument.class);
        AssetMapper mapper = Mappers.getMapper(AssetMapper.class);

        return mapper.mapper(doc);
    }

    default Wallet getWallet(TransactionDocument document) throws Exception {
        DocumentReference docRef = document.getWallet();
        ApiFuture<DocumentSnapshot> query = docRef.get();
        DocumentSnapshot snapshot = query.get();

        WalletDocument doc = snapshot.toObject(WalletDocument.class);
        WalletMapper mapper = Mappers.getMapper(WalletMapper.class);

        return mapper.mapper(doc);
    }

    default LocalDateTime parseToLocalDateTime(TransactionDocument document){

        String timestamp = (!Objects.isNull(document.getDatetime()))?
                document.getDatetime().toString().substring(0, 19) :
                document.getDate();

        LocalDateTime dateTime = LocalDateTime.parse(timestamp);

        LocalDateTime convertedDateTime = LocalDateTimeConverter.convertTimeZone(ZoneId.of("UTC"), ZoneId.systemDefault(), dateTime);

        return convertedDateTime;
    }

    default Timestamp parseToTimestamp(LocalDateTime localDateTime){

        LocalDateTime convertedDateTime = LocalDateTimeConverter.convertTimeZone(ZoneId.systemDefault(), ZoneId.of("UTC"), localDateTime);

        Timestamp timestamp = Timestamp.parseTimestamp(convertedDateTime.toString());

        return timestamp;
    }
}
