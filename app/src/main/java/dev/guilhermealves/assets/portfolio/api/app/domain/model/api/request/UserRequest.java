package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRequest {

    @Schema(description = "ID do usuário (opcional na criação)", example = "user-uuid")
    private String id;

    @NotBlank(message = "O nome é obrigatório")
    @Schema(description = "Nome completo do usuário", example = "Guilherme Alves")
    private String name;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O e-mail deve ser válido")
    @Schema(description = "E-mail de cadastro", example = "usuario@exemplo.com")
    private String email;

    @Schema(description = "Tipo de perfil do usuário (NORMAL ou ADMIN, padrão: NORMAL)", example = "NORMAL")
    private UserRole role;
}
