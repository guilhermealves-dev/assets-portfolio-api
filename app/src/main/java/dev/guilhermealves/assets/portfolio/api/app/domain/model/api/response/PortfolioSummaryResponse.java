package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortfolioSummaryResponse {

    @Schema(description = "Saldo total de mercado somando todas as carteiras", example = "500000.00")
    private BigDecimal totalBalance;

    @Schema(description = "Total financeiro investido somando todas as carteiras", example = "420000.00")
    private BigDecimal totalInvested;

    @Schema(description = "Lucro ou prejuízo não realizado geral", example = "80000.00")
    private BigDecimal unrealizedProfitLoss;

    @Schema(description = "Percentual de lucro ou prejuízo não realizado geral", example = "19.05")
    private BigDecimal unrealizedProfitLossPercentage;

    @Schema(description = "Lucro ou prejuízo já realizado em todas as vendas", example = "25000.00")
    private BigDecimal realizedProfitLoss;

    @Schema(description = "Lucro total de todo o histórico (aberto + fechado)", example = "105000.00")
    private BigDecimal allTimeProfitLoss;

    @Schema(description = "Quantidade total de carteiras no portfólio", example = "3")
    private Integer totalWalletsCount;

    @Schema(description = "Quantidade de ativos distintos mantidos em custódia no portfólio", example = "8")
    private Integer totalAssetsCount;

    @Schema(description = "Distribuição percentual global por classe de ativo")
    private Map<AssetType, BigDecimal> allocationByType;

    @Schema(description = "Ativo com melhor performance percentual geral do portfólio")
    private AssetPerformanceResponse bestPerformer;

    @Schema(description = "Ativo com pior performance percentual geral do portfólio")
    private AssetPerformanceResponse worstPerformer;

    @Schema(description = "Resumo detalhado de cada carteira")
    private List<WalletSummaryResponse> wallets;

    @Schema(description = "Moeda dos valores exibidos", example = "USD")
    private String currency;
}
