package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição para troca direta (SWAP) entre dois ativos dentro de uma carteira")
public class WalletSwapRequest {

    @Schema(description = "ID da carteira onde a troca será realizada (opcional se fornecido na URL)", example = "wallet-uuid")
    private String walletId;

    @NotBlank(message = "O símbolo ou ID do ativo de origem (fromAsset) deve ser informado")
    @Schema(description = "Símbolo ou ID do ativo que está sendo entregue na troca", example = "ETH")
    private String fromAsset;

    @NotNull(message = "A quantidade do ativo de origem é obrigatória")
    @Positive(message = "A quantidade do ativo de origem deve ser maior que zero")
    @Schema(description = "Quantidade do ativo de origem entregue", example = "1.0")
    private BigDecimal fromQuantity;

    @NotBlank(message = "O símbolo ou ID do ativo de destino (toAsset) deve ser informado")
    @Schema(description = "Símbolo ou ID do ativo que está sendo recebido na troca", example = "SOL")
    private String toAsset;

    @NotNull(message = "A quantidade do ativo de destino é obrigatória")
    @Positive(message = "A quantidade do ativo de destino deve ser maior que zero")
    @Schema(description = "Quantidade do ativo de destino recebida", example = "15.5")
    private BigDecimal toQuantity;

    @PositiveOrZero(message = "A taxa não pode ser negativa")
    @Schema(description = "Taxa paga na operação de troca (opcional)", example = "2.00")
    private BigDecimal fee;

    @Schema(description = "Data e hora da troca no formato ISO (opcional, padrão agora)", example = "2026-11-12T16:00:00")
    private String datetime;

    @Schema(description = "Observações sobre a troca", example = "Swap ETH por SOL via DEX")
    private String notes;
}
