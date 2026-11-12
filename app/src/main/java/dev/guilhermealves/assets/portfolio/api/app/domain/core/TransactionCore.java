package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import com.google.cloud.firestore.DocumentReference;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.AssetFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.TransactionFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.WalletFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.TransactionDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.WalletDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.mapper.TransactionMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.TransactionRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.TransactionResponse;
import dev.guilhermealves.assets.portfolio.api.app.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionCore {

    private final TransactionMapper transactionMapper;
    private final TransactionFireBaseAdapter transactionFireBaseAdapter;
    private final WalletFireBaseAdapter walletFireBaseAdapter;
    private final AssetFireBaseAdapter assetFireBaseAdapter;
    private final WalletCore walletCore;
    private final SecurityCore securityCore;

    public TransactionResponse create(TransactionRequest transaction) throws Exception {
        // Valida se a carteira existe e pertence ao usuário autenticado
        WalletDocument walletDoc = walletFireBaseAdapter.findById(transaction.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Carteira", transaction.getWalletId()));
        walletCore.validateWalletOwnership(walletDoc);

        // Valida se o ativo existe
        assetFireBaseAdapter.findById(transaction.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Ativo", transaction.getAssetId()));

        TransactionDocument docRequest = transactionMapper.map(transaction);

        DocumentReference wallet = walletFireBaseAdapter.getDocReference(transaction.getWalletId());
        DocumentReference asset = assetFireBaseAdapter.getDocReference(transaction.getAssetId());

        docRequest.setWallet(wallet);
        docRequest.setAsset(asset);
        docRequest.setWalletId(transaction.getWalletId());
        docRequest.setAssetId(transaction.getAssetId());

        TransactionDocument doc = transactionFireBaseAdapter.create(docRequest);
        return transactionMapper.map(doc);
    }

    public TransactionResponse update(TransactionRequest transaction) throws Exception {
        TransactionDocument existingTx = transactionFireBaseAdapter.findById(transaction.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transação", transaction.getId()));
        validateTransactionOwnership(existingTx);

        if (transaction.getWalletId() != null) {
            WalletDocument targetWallet = walletFireBaseAdapter.findById(transaction.getWalletId())
                    .orElseThrow(() -> new ResourceNotFoundException("Carteira", transaction.getWalletId()));
            walletCore.validateWalletOwnership(targetWallet);
        }

        if (transaction.getAssetId() != null) {
            assetFireBaseAdapter.findById(transaction.getAssetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ativo", transaction.getAssetId()));
        }

        TransactionDocument docRequest = transactionMapper.map(transaction);

        String wId = transaction.getWalletId() != null ? transaction.getWalletId() : existingTx.getWalletId();
        String aId = transaction.getAssetId() != null ? transaction.getAssetId() : existingTx.getAssetId();

        DocumentReference wallet = walletFireBaseAdapter.getDocReference(wId);
        DocumentReference asset = assetFireBaseAdapter.getDocReference(aId);

        docRequest.setWallet(wallet);
        docRequest.setAsset(asset);
        docRequest.setWalletId(wId);
        docRequest.setAssetId(aId);

        TransactionDocument doc = transactionFireBaseAdapter.update(docRequest);
        return transactionMapper.map(doc);
    }

    public List<TransactionResponse> findAll() throws Exception {
        Set<String> userWalletIds = getUserWalletIds();
        List<TransactionDocument> allTxs = transactionFireBaseAdapter.findAll();

        List<TransactionDocument> userTxs = allTxs.stream()
                .filter(tx -> isTxInWallets(tx, userWalletIds))
                .collect(Collectors.toList());

        return transactionMapper.mapList(userTxs);
    }

    public List<TransactionResponse> findByFilter(String walletId, String assetId, Integer page, Integer size) throws Exception {
        Set<String> userWalletIds = getUserWalletIds();

        if (walletId != null && !walletId.isBlank()) {
            WalletDocument walletDoc = walletFireBaseAdapter.findById(walletId)
                    .orElseThrow(() -> new ResourceNotFoundException("Carteira", walletId));
            walletCore.validateWalletOwnership(walletDoc);
        }

        List<TransactionDocument> transactionDocs = transactionFireBaseAdapter.findWithFilter(walletId, assetId, page, size);
        List<TransactionDocument> userTxs = transactionDocs.stream()
                .filter(tx -> isTxInWallets(tx, userWalletIds))
                .collect(Collectors.toList());

        return transactionMapper.mapList(userTxs);
    }

    public Optional<TransactionResponse> findById(String id) throws Exception {
        Optional<TransactionDocument> optionalDoc = transactionFireBaseAdapter.findById(id);

        if (optionalDoc.isPresent()) {
            validateTransactionOwnership(optionalDoc.get());
            return Optional.of(transactionMapper.map(optionalDoc.get()));
        }

        return Optional.empty();
    }

    public void deleteById(String id) throws Exception {
        TransactionDocument existingTx = transactionFireBaseAdapter.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação", id));
        validateTransactionOwnership(existingTx);

        transactionFireBaseAdapter.deleteById(id);
    }

    private void validateTransactionOwnership(TransactionDocument tx) throws Exception {
        String wId = tx.getWalletId() != null ? tx.getWalletId() :
                (tx.getWallet() != null ? tx.getWallet().getId() : null);

        if (wId != null) {
            WalletDocument wallet = walletFireBaseAdapter.findById(wId)
                    .orElseThrow(() -> new ResourceNotFoundException("Carteira", wId));
            walletCore.validateWalletOwnership(wallet);
        }
    }

    private Set<String> getUserWalletIds() throws Exception {
        String currentUserId = securityCore.getCurrentUserId();
        return walletFireBaseAdapter.findAll().stream()
                .filter(w -> w.getUser() != null && currentUserId.equals(w.getUser().getId()))
                .map(WalletDocument::getId)
                .collect(Collectors.toSet());
    }

    private boolean isTxInWallets(TransactionDocument tx, Set<String> walletIds) {
        String wId = tx.getWalletId() != null ? tx.getWalletId() :
                (tx.getWallet() != null ? tx.getWallet().getId() : null);
        return wId != null && walletIds.contains(wId);
    }
}
