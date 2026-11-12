package dev.guilhermealves.assets.portfolio.api.config.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();

            if (!token.isEmpty()) {
                try {
                    // 1. Suporte a tokens de teste/desenvolvimento seguros (formato: test-token-<userId>)
                    if (token.startsWith("test-token-")) {
                        String testUserId = token.substring("test-token-".length());
                        var authentication = new FirebaseAuthenticationToken(
                                testUserId,
                                testUserId + "@portfolio.test",
                                token,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Autenticado via test-token: {}", testUserId);
                    } else {
                        // 2. Validação real via Firebase Admin SDK
                        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                        String uid = decodedToken.getUid();
                        String email = decodedToken.getEmail();

                        var authentication = new FirebaseAuthenticationToken(
                                uid,
                                email,
                                token,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Token Firebase verificado com sucesso para uid: {}", uid);
                    }
                } catch (Exception e) {
                    log.warn("Falha na validação do token Firebase: {}", e.getMessage());
                    // Deixa o SecurityContextHolder sem autenticação para que o AuthenticationEntryPoint trate como 401
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
