package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WalletRequest {

    @Schema(description = "ID da carteira (opcional na criação)", example = "wallet-uuid")
    private String id;

    @NotBlank(message = "O nome da carteira é obrigatório")
    @Schema(description = "Nome descritivo da carteira", example = "Minha Carteira Cripto")
    private String name;

    @Schema(description = "ID do usuário proprietário da carteira", example = "user-uuid")
    private String userId;

    @NotBlank(message = "O ícone da carteira é obrigatório")
    @Schema(description = "Identificador ou URL do ícone", example = "wallet-icon")
    private String icon;

    @NotBlank(message = "A cor da carteira é obrigatória")
    @Schema(description = "Código hexadecimal da cor", example = "#F7931A")
    private String color;
}
