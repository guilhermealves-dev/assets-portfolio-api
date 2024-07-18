package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import com.google.cloud.firestore.DocumentReference;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.AssetFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.TransactionFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.WalletFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.TransactionDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.mapper.TransactionMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.TransactionRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionCore {

    private final TransactionMapper transactionMapper;
    private final TransactionFireBaseAdapter transactionFireBaseAdapter;
    private final WalletFireBaseAdapter walletFireBaseAdapter;
    private final AssetFireBaseAdapter assetFireBaseAdapter;

    public TransactionResponse create(TransactionRequest transaction) throws Exception {
        try {
            TransactionDocument docRequest = transactionMapper.map(transaction);

            DocumentReference wallet = walletFireBaseAdapter.getDocReference(transaction.getWalletId());
            DocumentReference asset = assetFireBaseAdapter.getDocReference(transaction.getAssetId());

            docRequest.setWallet(wallet);
            docRequest.setAsset(asset);

            TransactionDocument doc = transactionFireBaseAdapter.create(docRequest);

            return transactionMapper.map(doc);

        } catch (Throwable t){
            log.error("Error on create - {}", t.getMessage());
            throw t;
        }
    }

    public TransactionResponse update(TransactionRequest transaction) throws Exception {
        try {
            TransactionDocument docRequest = transactionMapper.map(transaction);

            DocumentReference wallet = walletFireBaseAdapter.getDocReference(transaction.getWalletId());
            DocumentReference asset = assetFireBaseAdapter.getDocReference(transaction.getAssetId());

            docRequest.setWallet(wallet);
            docRequest.setAsset(asset);

            TransactionDocument doc = transactionFireBaseAdapter.update(docRequest);

            return transactionMapper.map(doc);

        } catch (Throwable t){
            log.error("Error on update - {}", t.getMessage());
            throw t;
        }
    }

    public List<TransactionResponse> findAll() throws Exception {
        try {
            List<TransactionDocument> transactionDocs = transactionFireBaseAdapter.findAll();

            return transactionMapper.mapList(transactionDocs);

        } catch (Throwable t){
            log.error("Error on find All - {}", t.getMessage());
            throw t;
        }
    }

    public Optional<TransactionResponse> findById(String id) throws Exception {
        try {
            Optional<TransactionDocument> optionalDoc = transactionFireBaseAdapter.findById(id);

            if(optionalDoc.isPresent()){
                return Optional.of(transactionMapper.map(optionalDoc.get()));
            }

            return Optional.empty();

        } catch (Throwable t){
            log.error("Error on find By Id - {}", t.getMessage());
            throw t;
        }
    }

    public void deleteById(String id) throws Exception {
        try {
            transactionFireBaseAdapter.deleteById(id);
        } catch (Throwable t){
            log.error("Error on delete By Id - {}", t.getMessage());
            throw t;
        }
    }
}
