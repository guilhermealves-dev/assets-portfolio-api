package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionRequest {

    private String id;

    @NotBlank
    private String type;

    @NotBlank
    private String assetId;

    @NotBlank
    private String walletId;

    @NotNull
    private BigDecimal price;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private BigDecimal quantity;

    @NotNull
    private BigDecimal fee;

    @NotBlank
    private String datetime;
}
