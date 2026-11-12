package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.UserFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.UserDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.mapper.UserMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.UserResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.UserRole;
import dev.guilhermealves.assets.portfolio.api.config.security.FirebaseAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityCore {

    private final Firestore dbFirestore;
    private final UserMapper userMapper;
    private final UserFireBaseAdapter userFireBaseAdapter;

    /**
     * Retorna o ID único (uid) do usuário autenticado no token da requisição.
     */
    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("Usuário não autenticado");
        }
        return auth.getName();
    }

    /**
     * Retorna os dados completos do usuário autenticado buscando no Firestore ou no token.
     */
    public UserResponse getCurrentUser() throws Exception {
        String uid = getCurrentUserId();
        Optional<UserDocument> opUser = userFireBaseAdapter.findById(uid);
        if (opUser.isPresent()) {
            return userMapper.map(opUser.get());
        }

        // Se o usuário ainda não possui documento explícito no Firestore, sintetiza a partir do token
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = null;
        if (auth instanceof FirebaseAuthenticationToken) {
            email = ((FirebaseAuthenticationToken) auth).getEmail();
        }

        return UserResponse.builder()
                .id(uid)
                .name("Investidor " + uid.substring(0, Math.min(8, uid.length())))
                .email(email)
                .role(isCurrentUserAdmin() ? UserRole.ADMIN : UserRole.NORMAL)
                .build();
    }

    /**
     * Verifica se o usuário autenticado possui perfil de Administrador (ADMIN).
     */
    public boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));
    }

    /**
     * Retorna a referência Firestore DocumentReference do usuário autenticado.
     */
    public DocumentReference getCurrentUserDoc() throws Exception {
        String uid = getCurrentUserId();
        return dbFirestore.collection(UserFireBaseAdapter.COLLECTION_NAME).document(uid);
    }
}
