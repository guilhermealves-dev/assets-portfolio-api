package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.AssetFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.TransactionFireBaseAdapter;
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

        // 2. Migrar Ativos (Assets)
        ApiFuture<QuerySnapshot> assetFuture = dbFirestore.collection(AssetFireBaseAdapter.COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> assetDocs = assetFuture.get().getDocuments();

        for (QueryDocumentSnapshot doc : assetDocs) {
            String type = doc.getString("type");
            if (type != null && !type.equals(type.toUpperCase())) {
                batch.update(doc.getReference(), "type", type.toUpperCase());
                migratedAssets++;
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

        log.info("Migração concluída com sucesso! Transações atualizadas: {}, Ativos atualizados: {}",
                migratedTransactions, migratedAssets);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "SUCCESS");
        result.put("migratedTransactions", migratedTransactions);
        result.put("migratedAssets", migratedAssets);
        result.put("message", "Dados históricos normalizados e atualizados com sucesso no Firebase.");

        return result;
    }
}
