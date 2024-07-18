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
public class AssetRequest {

    private String id;

    @NotBlank
    private String symbol;

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    @NotBlank
    private String imgUrl;
}
