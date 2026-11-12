package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {

    @Schema(description = "ID do usuário", example = "user-uuid")
    private String id;

    @Schema(description = "Nome do usuário", example = "Guilherme Alves")
    private String name;

    @Schema(description = "E-mail do usuário", example = "usuario@exemplo.com")
    private String email;
}
