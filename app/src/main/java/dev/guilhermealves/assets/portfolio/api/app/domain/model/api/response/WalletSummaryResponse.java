package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WalletSummaryResponse {

    @Schema(description = "ID da carteira", example = "wallet-uuid")
    private String walletId;

    @Schema(description = "Nome da carteira", example = "Minha Carteira Principal")
    private String walletName;

    @Schema(description = "Saldo total consolidado a mercado", example = "250000.00")
    private BigDecimal totalBalance;

    @Schema(description = "Custo financeiro líquido total investido", example = "200000.00")
    private BigDecimal totalInvested;

    @Schema(description = "Lucro ou prejuízo não realizado (aberto)", example = "50000.00")
    private BigDecimal unrealizedProfitLoss;

    @Schema(description = "Percentual de lucro ou prejuízo não realizado", example = "25.00")
    private BigDecimal unrealizedProfitLossPercentage;

    @Schema(description = "Lucro ou prejuízo já realizado em vendas fechadas", example = "12000.00")
    private BigDecimal realizedProfitLoss;

    @Schema(description = "Lucro total histórico (não realizado + realizado)", example = "62000.00")
    private BigDecimal allTimeProfitLoss;

    @Schema(description = "Percentual de lucro histórico total", example = "31.00")
    private BigDecimal allTimeProfitLossPercentage;

    @Schema(description = "Quantidade de ativos diferentes em custódia", example = "5")
    private Integer totalAssetsCount;

    @Schema(description = "Quantidade total de transações registradas", example = "18")
    private Integer totalTransactionsCount;

    @Schema(description = "Distribuição percentual por classe de ativo")
    private Map<AssetType, BigDecimal> allocationByType;

    @Schema(description = "Ativo com melhor performance percentual da carteira")
    private AssetPerformanceResponse bestPerformer;

    @Schema(description = "Ativo com pior performance percentual da carteira")
    private AssetPerformanceResponse worstPerformer;

    @Schema(description = "Moeda dos valores exibidos", example = "USD")
    private String currency;
}
