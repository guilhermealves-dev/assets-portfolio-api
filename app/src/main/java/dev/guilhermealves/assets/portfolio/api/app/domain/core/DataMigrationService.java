package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.AssetFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.TransactionFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.UserFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.WalletFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        // 3. Migrar Usuários (garantir atributo role em todos os usuários e limpar usuários de teste legados)
        int migratedUsers = 0;
        int deletedTestUsers = 0;
        int deletedTestWallets = 0;
        Set<String> testUserIds = new HashSet<>();

        ApiFuture<QuerySnapshot> userFuture = dbFirestore.collection(UserFireBaseAdapter.COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> userDocs = userFuture.get().getDocuments();

        for (QueryDocumentSnapshot doc : userDocs) {
            String email = doc.getString("email");
            if (email != null && (email.endsWith("@portfolio.dev") || email.endsWith("@portfolio.test") || email.startsWith("user.b."))) {
                testUserIds.add(doc.getId());
                batch.delete(doc.getReference());
                deletedTestUsers++;
                batchCount++;
            } else {
                String role = doc.getString("role");
                if (role == null || role.trim().isEmpty()) {
                    batch.update(doc.getReference(), "role", UserRole.NORMAL.name());
                    migratedUsers++;
                    batchCount++;
                }
            }

            if (batchCount >= 400) {
                batch.commit().get();
                batch = dbFirestore.batch();
                batchCount = 0;
            }
        }

        // Limpar carteiras órfãs pertencentes a usuários de teste
        if (!testUserIds.isEmpty()) {
            try {
                ApiFuture<QuerySnapshot> walletFuture = dbFirestore.collection(WalletFireBaseAdapter.COLLECTION_NAME).get();
                List<QueryDocumentSnapshot> walletDocs = walletFuture.get().getDocuments();
                for (QueryDocumentSnapshot wDoc : walletDocs) {
                    String uId = wDoc.getString("userId");
                    if (uId != null && testUserIds.contains(uId)) {
                        batch.delete(wDoc.getReference());
                        deletedTestWallets++;
                        batchCount++;
                    }

                    if (batchCount >= 400) {
                        batch.commit().get();
                        batch = dbFirestore.batch();
                        batchCount = 0;
                    }
                }
            } catch (Exception e) {
                log.warn("Erro ao limpar carteiras de teste: {}", e.getMessage());
            }
        }

        if (batchCount > 0) {
            batch.commit().get();
        }

        log.info("Migração concluída com sucesso! Transações: {}, Ativos: {}, Usuários com role atualizada: {}, Usuários de teste removidos: {}, Carteiras de teste removidas: {}",
                migratedTransactions, migratedAssets, migratedUsers, deletedTestUsers, deletedTestWallets);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "SUCCESS");
        result.put("migratedTransactions", migratedTransactions);
        result.put("migratedAssets", migratedAssets);
        result.put("migratedUsers", migratedUsers);
        result.put("deletedTestUsers", deletedTestUsers);
        result.put("deletedTestWallets", deletedTestWallets);
        result.put("message", "Dados históricos normalizados e usuários de teste órfãos removidos com sucesso no Firebase.");

        return result;
    }
}
