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
@Schema(description = "Resultado da operação de troca direta (SWAP)")
public class WalletSwapResponse {

    @Schema(description = "ID da carteira onde ocorreu o swap", example = "wallet-uuid")
    private String walletId;

    @Schema(description = "Nome da carteira onde ocorreu o swap", example = "Carteira Principal")
    private String walletName;

    @Schema(description = "Símbolo do ativo de origem", example = "ETH")
    private String fromSymbol;

    @Schema(description = "Quantidade entregue do ativo de origem", example = "1.0")
    private BigDecimal fromQuantity;

    @Schema(description = "Símbolo do ativo de destino", example = "SOL")
    private String toSymbol;

    @Schema(description = "Quantidade recebida do ativo de destino", example = "15.5")
    private BigDecimal toQuantity;

    @Schema(description = "Lucro ou prejuízo realizado na venda/saída do ativo de origem", example = "120.50")
    private BigDecimal realizedProfitLoss;

    @Schema(description = "Transação de saída (SELL / SWAP OUT) do ativo entregue")
    private TransactionResponse fromTransaction;

    @Schema(description = "Transação de entrada (BUY / SWAP IN) do ativo recebido")
    private TransactionResponse toTransaction;

    @Schema(description = "Mensagem descritiva do resultado da operação")
    private String message;
}
