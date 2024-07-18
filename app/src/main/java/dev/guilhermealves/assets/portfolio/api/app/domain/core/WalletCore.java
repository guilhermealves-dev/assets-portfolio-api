package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import dev.guilhermealves.assets.portfolio.api.app.adapters.out.WalletFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.WalletDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.mapper.WalletMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.WalletRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.WalletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletCore {

    private final WalletFireBaseAdapter walletFireBaseAdapter;
    private final WalletMapper walletMapper;
    private final SecurityCore securityCore;

    public WalletResponse create(WalletRequest wallet) throws Exception {
        try {
            WalletDocument docRequest = walletMapper.map(wallet);
            docRequest.setUser(securityCore.getCurrentUserDoc());

            WalletDocument doc = walletFireBaseAdapter.create(docRequest);

            return walletMapper.map(doc);

        } catch (Throwable t){
            log.error("Error on create - {}", t.getMessage());
            throw t;
        }
    }

    public WalletResponse update(WalletRequest wallet) throws Exception {
        try {
            WalletDocument docRequest = walletMapper.map(wallet);

            docRequest.setUser(securityCore.getCurrentUserDoc());

            WalletDocument doc = walletFireBaseAdapter.update(docRequest);

            return walletMapper.map(doc);

        } catch (Throwable t){
            log.error("Error on update - {}", t.getMessage());
            throw t;
        }
    }

    public List<WalletResponse> findAll() throws Exception {
        try {
            List<WalletDocument> walletDocs = walletFireBaseAdapter.findAll();

            return walletMapper.mapList(walletDocs);

        } catch (Throwable t){
            log.error("Error on find All - {}", t.getMessage());
            throw t;
        }
    }

    public Optional<WalletResponse> findById(String id) throws Exception {
        try {
            Optional<WalletDocument> optionalDoc = walletFireBaseAdapter.findById(id);

            if(optionalDoc.isPresent()){
                return Optional.of(walletMapper.map(optionalDoc.get()));
            }

            return Optional.empty();

        } catch (Throwable t){
            log.error("Error on find By Id - {}", t.getMessage());
            throw t;
        }
    }

    public void deleteById(String id) throws Exception {
        try {
            walletFireBaseAdapter.deleteById(id);
        } catch (Throwable t){
            log.error("Error on delete By Id - {}", t.getMessage());
            throw t;
        }
    }
}
