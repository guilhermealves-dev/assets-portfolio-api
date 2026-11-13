package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
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
@Schema(description = "Requisição para transferência de ativos entre carteiras do mesmo usuário")
public class WalletTransferRequest {

    @NotBlank(message = "O ID da carteira de origem (sourceWalletId) é obrigatório")
    @Schema(description = "ID da carteira de origem", example = "wallet-source-id")
    private String sourceWalletId;

    @NotBlank(message = "O ID da carteira de destino (targetWalletId) é obrigatório")
    @Schema(description = "ID da carteira de destino", example = "wallet-target-id")
    private String targetWalletId;

    @Schema(description = "ID do ativo a transferir (opcional se assetSymbol for informado)", example = "asset-uuid")
    private String assetId;

    @Schema(description = "Símbolo/Ticker do ativo a transferir (ex: BTC, ETH, PETR4)", example = "BTC")
    private String assetSymbol;

    @NotNull(message = "A quantidade a transferir é obrigatória")
    @Positive(message = "A quantidade transferida deve ser maior que zero")
    @Schema(description = "Quantidade de cotas/moedas a transferir", example = "0.5")
    private BigDecimal quantity;

    @PositiveOrZero(message = "A taxa não pode ser negativa")
    @Schema(description = "Taxa paga na transferência da carteira de origem (opcional)", example = "5.00")
    private BigDecimal fee;

    @Schema(description = "Data e hora da transferência em formato ISO (opcional, padrão agora)", example = "2026-11-12T15:30:00")
    private String datetime;

    @Schema(description = "Observações ou notas adicionais sobre a transferência", example = "Transferência para carteira de hold")
    private String notes;

    @JsonIgnore
    @AssertTrue(message = "O ID do ativo (assetId) ou o símbolo (assetSymbol) deve ser informado")
    public boolean isAssetSpecified() {
        return (assetId != null && !assetId.isBlank()) || (assetSymbol != null && !assetSymbol.isBlank());
    }

    @JsonIgnore
    public String getEffectiveAssetIdentifier() {
        if (assetId != null && !assetId.isBlank()) {
            return assetId.trim();
        }
        if (assetSymbol != null && !assetSymbol.isBlank()) {
            return assetSymbol.trim();
        }
        return null;
    }
}
