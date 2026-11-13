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
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.TransactionType;
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
            @Mapping(target = "walletId", source = "walletId"),
            @Mapping(target = "assetId", source = "assetId"),
            @Mapping(target = "type", expression = "java(request.getType() != null ? request.getType().name() : null)"),
            @Mapping(target = "datetime", expression = "java(parseToTimestamp(request))")
    })
    TransactionDocument map(TransactionRequest request);

    @Mappings({
            @Mapping(target = "type", expression = "java(parseTransactionType(document.getType()))"),
            @Mapping(target = "asset", expression = "java(getAsset(document))"),
            @Mapping(target = "wallet", expression = "java(getWallet(document))"),
            @Mapping(target = "datetime", expression = "java(parseToLocalDateTime(document))")
    })
    TransactionResponse map(TransactionDocument document) throws Exception;

    List<TransactionResponse> mapList(List<TransactionDocument> documents);

    default TransactionType parseTransactionType(String type) {
        return TransactionType.fromString(type);
    }

    default AssetResponse getAsset(TransactionDocument document) throws Exception {
        DocumentReference docRef = document.getAsset();
        if (docRef == null) {
            return null;
        }

        ApiFuture<DocumentSnapshot> query = docRef.get();
        DocumentSnapshot snapshot = query.get();

        if (!snapshot.exists()) {
            return null;
        }

        AssetDocument doc = snapshot.toObject(AssetDocument.class);
        AssetMapper assetMapper = Mappers.getMapper(AssetMapper.class);

        return assetMapper.map(doc);
    }

    default WalletResponse getWallet(TransactionDocument document) throws Exception {
        DocumentReference docRef = document.getWallet();
        if (docRef == null) {
            return null;
        }

        ApiFuture<DocumentSnapshot> query = docRef.get();
        DocumentSnapshot snapshot = query.get();

        if (!snapshot.exists()) {
            return null;
        }

        WalletDocument doc = snapshot.toObject(WalletDocument.class);
        WalletMapper walletMapper = Mappers.getMapper(WalletMapper.class);

        return walletMapper.map(doc);
    }

    default LocalDateTime parseToLocalDateTime(TransactionDocument document) {
        if (document.getDatetime() == null) {
            return null;
        }

        String timestamp = document.getDatetime().toString().substring(0, 19);
        LocalDateTime dateTime = LocalDateTime.parse(timestamp);

        return LocalDateTimeConverter.convertTimeZone(ZoneId.of("UTC"), ZoneId.systemDefault(), dateTime);
    }

    default Timestamp parseToTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }

        LocalDateTime convertedDateTime = LocalDateTimeConverter.convertTimeZone(ZoneId.systemDefault(), ZoneId.of("UTC"), localDateTime);
        String s = convertedDateTime.toString();
        return Timestamp.parseTimestamp(s.endsWith("Z") ? s : s + "Z");
    }

    default Timestamp parseToTimestamp(TransactionRequest request) {
        if (request.getDatetime() == null || request.getDatetime().isBlank()) {
            return Timestamp.now();
        }

        String dt = request.getDatetime().trim();
        try {
            if (dt.endsWith("Z") || dt.contains("+") || (dt.contains("-") && dt.lastIndexOf("-") > 10)) {
                java.time.Instant instant = java.time.Instant.parse(dt);
                return Timestamp.ofTimeSecondsAndNanos(instant.getEpochSecond(), instant.getNano());
            }

            LocalDateTime localDateTime = LocalDateTime.parse(dt);
            LocalDateTime convertedDateTime = LocalDateTimeConverter.convertTimeZone(ZoneId.systemDefault(), ZoneId.of("UTC"), localDateTime);
            String s = convertedDateTime.toString();
            return Timestamp.parseTimestamp(s.endsWith("Z") ? s : s + "Z");
        } catch (Exception e) {
            try {
                return Timestamp.parseTimestamp(dt.endsWith("Z") ? dt : dt + "Z");
            } catch (Exception ex) {
                return Timestamp.now();
            }
        }
    }
}
