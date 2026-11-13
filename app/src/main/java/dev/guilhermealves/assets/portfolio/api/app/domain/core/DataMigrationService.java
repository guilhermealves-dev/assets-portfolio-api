package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.AssetFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.TransactionFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.UserFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataMigrationService {

    private final Firestore dbFirestore;
    private final AssetIconResolver assetIconResolver;

    public Map<String, Object> migrateExistingData() throws Exception {
        log.info("Iniciando migração e padronização dos dados no Firebase...");

        int migratedTransactions = 0;
        int migratedAssets = 0;

        // 1. Migrar Transações
        ApiFuture<QuerySnapshot> txFuture = dbFirestore.collection(TransactionFireBaseAdapter.COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> txDocs = txFuture.get().getDocuments();

        WriteBatch batch = dbFirestore.batch();
        int batchCount = 0;

        for (QueryDocumentSnapshot doc : txDocs) {
            Map<String, Object> updates = new HashMap<>();

            String type = doc.getString("type");
            if (type != null && !type.equals(type.toUpperCase())) {
                updates.put("type", type.toUpperCase());
            }

            // Normalizar walletId se estiver ausente
            if (!doc.contains("walletId") || doc.getString("walletId") == null) {
                if (doc.contains("wallet")) {
                    DocumentReference walletRef = doc.get("wallet", DocumentReference.class);
                    if (walletRef != null) {
                        updates.put("walletId", walletRef.getId());
                    }
                }
            }

            // Normalizar assetId se estiver ausente
            if (!doc.contains("assetId") || doc.getString("assetId") == null) {
                if (doc.contains("asset")) {
                    DocumentReference assetRef = doc.get("asset", DocumentReference.class);
                    if (assetRef != null) {
                        updates.put("assetId", assetRef.getId());
                    }
                }
            }

            if (!updates.isEmpty()) {
                batch.update(doc.getReference(), updates);
                migratedTransactions++;
                batchCount++;
            }

            // Commit a cada 400 ops (limite do Firestore é 500)
            if (batchCount >= 400) {
                batch.commit().get();
                batch = dbFirestore.batch();
                batchCount = 0;
            }
        }

        // 2. Migrar e Padronizar Ativos (Assets - types e URLs de logos)
        ApiFuture<QuerySnapshot> assetFuture = dbFirestore.collection(AssetFireBaseAdapter.COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> assetDocs = assetFuture.get().getDocuments();

        for (QueryDocumentSnapshot doc : assetDocs) {
            Map<String, Object> assetUpdates = new HashMap<>();

            String typeStr = doc.getString("type");
            AssetType type = AssetType.fromString(typeStr);
            if (typeStr != null && !typeStr.equals(typeStr.toUpperCase())) {
                assetUpdates.put("type", typeStr.toUpperCase());
            }

            String currentImgUrl = doc.getString("imgUrl");
            String symbol = doc.getString("symbol");
            String newImgUrl = assetIconResolver.resolveIconUrl(symbol, type);
            if (currentImgUrl == null || !currentImgUrl.equals(newImgUrl)) {
                assetUpdates.put("imgUrl", newImgUrl);
            }

            if (!assetUpdates.isEmpty()) {
                batch.update(doc.getReference(), assetUpdates);
                migratedAssets++;
                batchCount++;
            }

            if (batchCount >= 400) {
                batch.commit().get();
                batch = dbFirestore.batch();
                batchCount = 0;
            }
        }

        // 3. Migrar Usuários (garantir atributo role em todos os usuários)
        int migratedUsers = 0;
        ApiFuture<QuerySnapshot> userFuture = dbFirestore.collection(UserFireBaseAdapter.COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> userDocs = userFuture.get().getDocuments();

        for (QueryDocumentSnapshot doc : userDocs) {
            String role = doc.getString("role");
            if (role == null || role.trim().isEmpty()) {
                batch.update(doc.getReference(), "role", UserRole.NORMAL.name());
                migratedUsers++;
                batchCount++;
            }

            if (batchCount >= 400) {
                batch.commit().get();
                batch = dbFirestore.batch();
                batchCount = 0;
            }
        }

        if (batchCount > 0) {
            batch.commit().get();
        }

        log.info("Migração concluída com sucesso! Transações: {}, Ativos: {}, Usuários com role atualizada: {}",
                migratedTransactions, migratedAssets, migratedUsers);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "SUCCESS");
        result.put("migratedTransactions", migratedTransactions);
        result.put("migratedAssets", migratedAssets);
        result.put("migratedUsers", migratedUsers);
        result.put("message", "Dados históricos normalizados e atualizados com sucesso no Firebase.");

        return result;
    }
}
