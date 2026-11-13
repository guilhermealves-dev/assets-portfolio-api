package dev.guilhermealves.assets.portfolio.api.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.net.URI;
import java.time.Instant;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final FirebaseAuthFilter firebaseAuthFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configure(http))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        // Documentação Swagger / OpenAPI
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // Cotações e dados públicos de mercado
                        .requestMatchers("/market/**").permitAll()
                        // Catálogo de ativos (leitura e busca pública)
                        .requestMatchers(HttpMethod.GET, "/assets/**").permitAll()
                        // Cadastro inicial público de usuários
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()
                        // Operações administrativas e migração de dados exclusivas para ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Listagem global de usuários restrita a ADMIN
                        .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")
                        // Endpoints protegidos por autenticação
                        .requestMatchers(
                                "/wallets/**",
                                "/transactions/**",
                                "/portfolio/**",
                                "/watchlist/**",
                                "/users/me",
                                "/users/**"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.UNAUTHORIZED,
                    "Autenticação necessária. Envie um token Bearer válido no cabeçalho Authorization."
            );
            problem.setTitle("Não autorizado");
            problem.setType(URI.create("https://api.assets-portfolio.dev/errors/unauthorized"));
            problem.setProperty("timestamp", Instant.now());
            problem.setProperty("path", request.getRequestURI());

            response.getWriter().write(objectMapper.writeValueAsString(problem));
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.FORBIDDEN,
                    "Acesso negado: você não tem permissão para acessar ou modificar este recurso."
            );
            problem.setTitle("Acesso Negado");
            problem.setType(URI.create("https://api.assets-portfolio.dev/errors/forbidden"));
            problem.setProperty("timestamp", Instant.now());
            problem.setProperty("path", request.getRequestURI());

            response.getWriter().write(objectMapper.writeValueAsString(problem));
        };
    }
}
