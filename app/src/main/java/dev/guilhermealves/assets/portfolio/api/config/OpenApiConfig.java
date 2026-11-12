package dev.guilhermealves.assets.portfolio.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Insira o token JWT do Firebase ou o token de desenvolvimento: `test-token-<userId>`")
                        )
                )
                .info(new Info()
                        .title("Assets Portfolio API")
                        .version("1.0.0")
                        .description("REST API para gerenciamento e consolidação analítica de portfólio de ativos financeiros (Criptomoedas, Ações BR/US, ETFs, etc.), inspirada no CoinMarketCap Portfolio Tracker.")
                        .contact(new Contact()
                                .name("Guilherme Alves")
                                .url("https://github.com/guilhermealves-dev"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }
}
