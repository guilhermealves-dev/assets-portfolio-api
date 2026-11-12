package dev.guilhermealves.assets.portfolio.api.config.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.UserFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.UserDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseAuthFilter extends OncePerRequestFilter {

    private final UserFireBaseAdapter userFireBaseAdapter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();

            if (!token.isEmpty()) {
                try {
                    // 1. Suporte a tokens de teste/desenvolvimento (formato: test-token-<userId> ou test-token-<userId>:admin)
                    if (token.startsWith("test-token-")) {
                        String testUserId = token.substring("test-token-".length());
                        boolean isAdmin = false;

                        if (testUserId.equalsIgnoreCase("admin") || testUserId.endsWith(":admin")) {
                            isAdmin = true;
                            if (testUserId.endsWith(":admin")) {
                                testUserId = testUserId.substring(0, testUserId.length() - ":admin".length());
                            }
                        } else {
                            try {
                                Optional<UserDocument> uDoc = userFireBaseAdapter.findById(testUserId);
                                if (uDoc.isPresent() && "ADMIN".equalsIgnoreCase(uDoc.get().getRole())) {
                                    isAdmin = true;
                                }
                            } catch (Exception ignored) {
                            }
                        }

                        List<GrantedAuthority> authorities = buildAuthorities(isAdmin);

                        var authentication = new FirebaseAuthenticationToken(
                                testUserId,
                                testUserId + "@portfolio.test",
                                token,
                                authorities
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Autenticado via test-token: {} (admin: {})", testUserId, isAdmin);
                    } else {
                        // 2. Validação real via Firebase Admin SDK
                        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                        String uid = decodedToken.getUid();
                        String email = decodedToken.getEmail();

                        boolean isAdmin = false;
                        Object adminClaim = decodedToken.getClaims().get("admin");
                        Object roleClaim = decodedToken.getClaims().get("role");

                        if (Boolean.TRUE.equals(adminClaim) || "ADMIN".equalsIgnoreCase(String.valueOf(roleClaim))) {
                            isAdmin = true;
                        } else {
                            try {
                                Optional<UserDocument> uDoc = userFireBaseAdapter.findById(uid);
                                if (uDoc.isPresent() && "ADMIN".equalsIgnoreCase(uDoc.get().getRole())) {
                                    isAdmin = true;
                                }
                            } catch (Exception ignored) {
                            }
                        }

                        List<GrantedAuthority> authorities = buildAuthorities(isAdmin);

                        var authentication = new FirebaseAuthenticationToken(
                                uid,
                                email,
                                token,
                                authorities
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Token Firebase verificado com sucesso para uid: {} (admin: {})", uid, isAdmin);
                    }
                } catch (Exception e) {
                    log.warn("Falha na validação do token Firebase: {}", e.getMessage());
                    // Deixa o SecurityContextHolder sem autenticação para que o AuthenticationEntryPoint trate como 401
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private List<GrantedAuthority> buildAuthorities(boolean isAdmin) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (isAdmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        authorities.add(new SimpleGrantedAuthority("ROLE_NORMAL"));
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        return authorities;
    }
}
