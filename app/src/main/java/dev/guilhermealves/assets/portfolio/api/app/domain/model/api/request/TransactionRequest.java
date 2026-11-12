package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionRequest {

    @Schema(description = "ID da transação (opcional na criação)", example = "tx-uuid")
    private String id;

    @NotNull(message = "O tipo de transação é obrigatório (BUY, SELL, etc.)")
    @Schema(description = "Tipo da transação", example = "BUY")
    private TransactionType type;

    @NotBlank(message = "O ID do ativo é obrigatório")
    @Schema(description = "ID do ativo negociado", example = "btc-uuid")
    private String assetId;

    @NotBlank(message = "O ID da carteira é obrigatório")
    @Schema(description = "ID da carteira pertencente", example = "wallet-uuid")
    private String walletId;

    @NotNull(message = "O preço unitário é obrigatório")
    @Positive(message = "O preço deve ser maior que zero")
    @Schema(description = "Preço unitário no momento da transação", example = "60000.00")
    private BigDecimal price;

    @NotNull(message = "O valor total (amount) é obrigatório")
    @Positive(message = "O montante deve ser maior que zero")
    @Schema(description = "Valor total financeiro da operação", example = "15000.00")
    private BigDecimal amount;

    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser maior que zero")
    @Schema(description = "Quantidade de cotas/moedas negociadas", example = "0.25")
    private BigDecimal quantity;

    @NotNull(message = "A taxa da operação é obrigatória (pode ser 0)")
    @PositiveOrZero(message = "A taxa não pode ser negativa")
    @Schema(description = "Taxa paga na operação", example = "2.50")
    private BigDecimal fee;

    @NotBlank(message = "A data/hora é obrigatória (ISO-8601: YYYY-MM-DDTHH:mm:ss)")
    @Schema(description = "Data e hora da transação", example = "2026-11-12T13:00:00")
    private String datetime;
}
