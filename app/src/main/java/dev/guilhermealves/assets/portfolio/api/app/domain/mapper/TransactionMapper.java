package dev.guilhermealves.assets.portfolio.api.app.domain.mapper;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.AssetDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.TransactionDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.WalletDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.TransactionRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.AssetResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.TransactionResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.WalletResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.utils.LocalDateTimeConverter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mappings({
            @Mapping(target = "asset", ignore = true),
            @Mapping(target = "wallet", ignore = true),
            @Mapping(target = "datetime", expression = "java(parseToTimestamp(request))")
    })
    TransactionDocument map(TransactionRequest request);

    @Mappings({
            @Mapping(target = "asset", expression = "java(getAsset(document))"),
            @Mapping(target = "wallet", expression = "java(getWallet(document))"),
            @Mapping(target = "datetime", expression = "java(parseToLocalDateTime(document))")
    })
    TransactionResponse map(TransactionDocument document) throws Exception;

    List<TransactionResponse> mapList(List<TransactionDocument> documents);

    default AssetResponse getAsset(TransactionDocument document) throws Exception {
        DocumentReference docRef = document.getAsset();
        ApiFuture<DocumentSnapshot> query = docRef.get();
        DocumentSnapshot snapshot = query.get();

        AssetDocument doc = snapshot.toObject(AssetDocument.class);
        AssetMapper assetMapper = Mappers.getMapper(AssetMapper.class);

        return assetMapper.map(doc);
    }

    default WalletResponse getWallet(TransactionDocument document) throws Exception {
        DocumentReference docRef = document.getWallet();
        ApiFuture<DocumentSnapshot> query = docRef.get();
        DocumentSnapshot snapshot = query.get();

        WalletDocument doc = snapshot.toObject(WalletDocument.class);
        WalletMapper walletMapper = Mappers.getMapper(WalletMapper.class);

        return walletMapper.map(doc);
    }

    default LocalDateTime parseToLocalDateTime(TransactionDocument document){

        String timestamp = document.getDatetime().toString().substring(0, 19);

        LocalDateTime dateTime = LocalDateTime.parse(timestamp);

        LocalDateTime convertedDateTime = LocalDateTimeConverter.convertTimeZone(ZoneId.of("UTC"), ZoneId.systemDefault(), dateTime);

        return convertedDateTime;
    }

    default Timestamp parseToTimestamp(LocalDateTime localDateTime){

        LocalDateTime convertedDateTime = LocalDateTimeConverter.convertTimeZone(ZoneId.systemDefault(), ZoneId.of("UTC"), localDateTime);

        Timestamp timestamp = Timestamp.parseTimestamp(convertedDateTime.toString());

        return timestamp;
    }

    default Timestamp parseToTimestamp(TransactionRequest request){
        LocalDateTime localDateTime = LocalDateTime.parse(request.getDatetime());

        LocalDateTime convertedDateTime = LocalDateTimeConverter.convertTimeZone(ZoneId.systemDefault(), ZoneId.of("UTC"), localDateTime);

        Timestamp timestamp = Timestamp.parseTimestamp(convertedDateTime.toString());

        return timestamp;
    }
}
