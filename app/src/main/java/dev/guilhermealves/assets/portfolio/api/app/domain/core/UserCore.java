package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import dev.guilhermealves.assets.portfolio.api.app.adapters.out.UserFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.UserDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.mapper.UserMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.UserRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.UserResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.UserRole;
import dev.guilhermealves.assets.portfolio.api.app.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCore {

    private final UserFireBaseAdapter userFireBaseAdapter;
    private final UserMapper userMapper;
    private final SecurityCore securityCore;

    public UserResponse create(UserRequest user) throws Exception {
        UserDocument docRequest = userMapper.map(user);

        // Se quem está criando não for ADMIN autenticado, o perfil é forçado para NORMAL por segurança
        boolean isAdmin = false;
        try {
            isAdmin = securityCore.isCurrentUserAdmin();
        } catch (Exception ignored) {
            // Requisição pública/anônima de auto-cadastro
        }

        if (!isAdmin || user.getRole() == null) {
            docRequest.setRole(UserRole.NORMAL.name());
        } else {
            docRequest.setRole(user.getRole().name());
        }

        UserDocument doc = userFireBaseAdapter.create(docRequest);
        return userMapper.map(doc);
    }

    public UserResponse update(UserRequest user) throws Exception {
        UserDocument existing = userFireBaseAdapter.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", user.getId()));

        validateUserAccess(user.getId(), "Acesso negado: você não tem permissão para alterar os dados de outro usuário");

        UserDocument docRequest = userMapper.map(user);

        // Apenas ADMIN pode alterar o papel (role) de um usuário
        if (!securityCore.isCurrentUserAdmin()) {
            docRequest.setRole(existing.getRole());
        } else if (user.getRole() != null) {
            docRequest.setRole(user.getRole().name());
        } else {
            docRequest.setRole(existing.getRole());
        }

        UserDocument doc = userFireBaseAdapter.update(docRequest);
        return userMapper.map(doc);
    }

    public List<UserResponse> findAll() throws Exception {
        if (!securityCore.isCurrentUserAdmin()) {
            throw new AccessDeniedException("Acesso negado: apenas administradores podem listar todos os usuários do sistema");
        }
        List<UserDocument> userDocs = userFireBaseAdapter.findAll();
        return userMapper.mapList(userDocs);
    }

    public Optional<UserResponse> findById(String id) throws Exception {
        validateUserAccess(id, "Acesso negado: você não tem permissão para visualizar dados de outro usuário");

        Optional<UserDocument> optionalDoc = userFireBaseAdapter.findById(id);
        if (optionalDoc.isPresent()) {
            return Optional.of(userMapper.map(optionalDoc.get()));
        }
        return Optional.empty();
    }

    public void deleteById(String id) throws Exception {
        userFireBaseAdapter.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));

        validateUserAccess(id, "Acesso negado: apenas administradores podem excluir outros usuários");

        userFireBaseAdapter.deleteById(id);
    }

    /**
     * Permite a operação se o requisitante for ADMIN ou se for o próprio usuário atuando em seus dados.
     */
    private void validateUserAccess(String targetUserId, String errorMessage) {
        if (securityCore.isCurrentUserAdmin()) {
            return;
        }
        String currentUserId = securityCore.getCurrentUserId();
        if (!currentUserId.equals(targetUserId)) {
            log.warn("Acesso indevido: usuário {} tentou acessar recurso do usuário {}", currentUserId, targetUserId);
            throw new AccessDeniedException(errorMessage);
        }
    }
}
