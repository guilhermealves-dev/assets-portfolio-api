package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WalletRequest {

    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String userId;

    @NotBlank
    private String icon;

    @NotBlank
    private String color;
}
