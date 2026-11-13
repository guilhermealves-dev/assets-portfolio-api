package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado da operação de transferência de ativo entre carteiras")
public class WalletTransferResponse {

    @Schema(description = "Transação de saída registrada na carteira de origem (TRANSFER_OUT)")
    private TransactionResponse sourceTransaction;

    @Schema(description = "Transação de entrada registrada na carteira de destino (TRANSFER_IN)")
    private TransactionResponse targetTransaction;

    @Schema(description = "ID da carteira de origem", example = "wallet-source-id")
    private String sourceWalletId;

    @Schema(description = "Nome da carteira de origem", example = "Carteira Principal")
    private String sourceWalletName;

    @Schema(description = "ID da carteira de destino", example = "wallet-target-id")
    private String targetWalletId;

    @Schema(description = "Nome da carteira de destino", example = "Cold Wallet")
    private String targetWalletName;

    @Schema(description = "Símbolo/Ticker do ativo transferido", example = "BTC")
    private String assetSymbol;

    @Schema(description = "Quantidade transferida", example = "0.5")
    private BigDecimal quantity;

    @Schema(description = "Preço médio de aquisição transferido", example = "60000.00")
    private BigDecimal transferPrice;

    @Schema(description = "Valor total financeiro transferido", example = "30000.00")
    private BigDecimal totalAmount;

    @Schema(description = "Taxa total da operação", example = "5.00")
    private BigDecimal fee;
}
