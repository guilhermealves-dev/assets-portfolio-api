package dev.guilhermealves.assets.portfolio.api.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URI;
import java.time.Instant;
import java.util.List;

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
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        // Requisições Preflight CORS (OPTIONS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permite origens locais comuns (React, Next.js, Vue, Vite, Angular, Flutter Web) e padrões flexíveis
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:[*]",
                "https://localhost:[*]",
                "http://127.0.0.1:[*]",
                "https://127.0.0.1:[*]",
                "https://*.pages.dev",
                "https://*.vercel.app",
                "*"
        ));

        // Métodos HTTP aceitos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));

        // Cabeçalhos permitidos nas requisições do frontend
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Cabeçalhos que o frontend tem permissão de ler na resposta (ex: download de CSV)
        configuration.setExposedHeaders(List.of(
                "Authorization",
                "Content-Disposition",
                "Content-Type",
                "Link",
                "X-Total-Count"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1 hora de cache preflight no navegador para alta performance

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
