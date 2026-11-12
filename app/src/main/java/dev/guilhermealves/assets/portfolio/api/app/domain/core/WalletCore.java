package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import com.google.cloud.firestore.DocumentReference;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.UserFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.WalletFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.WalletDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.mapper.WalletMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.WalletRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.WalletResponse;
import dev.guilhermealves.assets.portfolio.api.app.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletCore {

    private final WalletFireBaseAdapter walletFireBaseAdapter;
    private final UserFireBaseAdapter userFireBaseAdapter;
    private final WalletMapper walletMapper;
    private final SecurityCore securityCore;

    public WalletResponse create(WalletRequest wallet) throws Exception {
        WalletDocument docRequest = walletMapper.map(wallet);
        docRequest.setId(wallet.getId());

        // Vincula estritamente a carteira ao usuário autenticado extraído do token
        String currentUserId = securityCore.getCurrentUserId();
        DocumentReference userDoc = userFireBaseAdapter.getDocReference(currentUserId);
        docRequest.setUser(userDoc);

        WalletDocument doc = walletFireBaseAdapter.create(docRequest);
        return walletMapper.map(doc);
    }

    public WalletResponse update(WalletRequest wallet) throws Exception {
        WalletDocument existing = walletFireBaseAdapter.findById(wallet.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Carteira", wallet.getId()));

        validateWalletOwnership(existing);

        WalletDocument docRequest = walletMapper.map(wallet);
        docRequest.setId(wallet.getId());
        docRequest.setUser(existing.getUser()); // Preserva o dono original

        WalletDocument doc = walletFireBaseAdapter.update(docRequest);
        return walletMapper.map(doc);
    }

    public List<WalletResponse> findAll() throws Exception {
        String currentUserId = securityCore.getCurrentUserId();
        List<WalletDocument> allWallets = walletFireBaseAdapter.findAll();

        // Filtro obrigatório de isolamento de dados: retorna apenas carteiras do usuário autenticado
        List<WalletDocument> userWallets = allWallets.stream()
                .filter(w -> w.getUser() != null && currentUserId.equals(w.getUser().getId()))
                .collect(Collectors.toList());

        return walletMapper.mapList(userWallets);
    }

    public Optional<WalletResponse> findById(String id) throws Exception {
        Optional<WalletDocument> optionalDoc = walletFireBaseAdapter.findById(id);
        if (optionalDoc.isPresent()) {
            validateWalletOwnership(optionalDoc.get());
            return Optional.of(walletMapper.map(optionalDoc.get()));
        }
        return Optional.empty();
    }

    public void deleteById(String id) throws Exception {
        WalletDocument walletDoc = walletFireBaseAdapter.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira", id));

        validateWalletOwnership(walletDoc);
        walletFireBaseAdapter.deleteById(id);
    }

    public void validateWalletOwnership(WalletDocument wallet) {
        String currentUserId = securityCore.getCurrentUserId();
        String ownerId = (wallet.getUser() != null) ? wallet.getUser().getId() : null;

        if (ownerId != null && !ownerId.equals(currentUserId)) {
            log.warn("Tentativa de acesso indevido à carteira {} pelo usuário {}", wallet.getId(), currentUserId);
            throw new AccessDeniedException("Acesso negado: esta carteira pertence a outro usuário");
        }
    }
}
