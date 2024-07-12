package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import com.google.cloud.firestore.DocumentReference;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.AssetFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.TransactionFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.WalletFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.TransactionDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.mapper.TransactionMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.Transaction;
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

    public Transaction create(TransactionDocument transactionDoc) throws Exception {
        try {
            DocumentReference wallet = walletFireBaseAdapter.getDocReference(transactionDoc.getWalletId());
            DocumentReference asset = assetFireBaseAdapter.getDocReference(transactionDoc.getAssetId());

            transactionDoc.setWallet(wallet);
            transactionDoc.setAsset(asset);

            TransactionDocument doc = transactionFireBaseAdapter.create(transactionDoc);

            return transactionMapper.mapper(doc);

        } catch (Throwable t){
            log.error("Error on create - {}", t.getMessage());
            throw t;
        }
    }

    public Transaction update(TransactionDocument transactionDoc) throws Exception {
        try {
            DocumentReference wallet = walletFireBaseAdapter.getDocReference(transactionDoc.getWalletId());
            DocumentReference asset = assetFireBaseAdapter.getDocReference(transactionDoc.getAssetId());

            transactionDoc.setWallet(wallet);
            transactionDoc.setAsset(asset);

            TransactionDocument doc = transactionFireBaseAdapter.update(transactionDoc);

            return transactionMapper.mapper(doc);

        } catch (Throwable t){
            log.error("Error on update - {}", t.getMessage());
            throw t;
        }
    }

    public List<Transaction> findAll() throws Exception {
        try {
            List<TransactionDocument> transactionDocs = transactionFireBaseAdapter.findAll();

            return transactionMapper.mapperTransactionList(transactionDocs);

        } catch (Throwable t){
            log.error("Error on find All - {}", t.getMessage());
            throw t;
        }
    }

    public Optional<Transaction> findById(String id) throws Exception {
        try {
            Optional<TransactionDocument> optionalDoc = transactionFireBaseAdapter.findById(id);

            if(optionalDoc.isPresent()){
                return Optional.of(transactionMapper.mapper(optionalDoc.get()));
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
