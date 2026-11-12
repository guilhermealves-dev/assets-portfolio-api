package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HoldingResponse {

    @Schema(description = "Dados do ativo")
    private AssetResponse asset;

    @Schema(description = "Quantidade total do ativo em custódia", example = "2.5")
    private BigDecimal quantity;

    @Schema(description = "Preço médio ponderado de compra (Break-even price)", example = "60000.00")
    private BigDecimal avgBuyPrice;

    @Schema(description = "Cotação atual do ativo", example = "68500.00")
    private BigDecimal currentPrice;

    @Schema(description = "Valor financeiro atual de mercado (quantity * currentPrice)", example = "171250.00")
    private BigDecimal totalValue;

    @Schema(description = "Custo financeiro total investido (quantity * avgBuyPrice)", example = "150000.00")
    private BigDecimal totalInvested;

    @Schema(description = "Lucro ou prejuízo não realizado em valor financeiro", example = "21250.00")
    private BigDecimal unrealizedProfitLoss;

    @Schema(description = "Percentual de lucro ou prejuízo não realizado", example = "14.17")
    private BigDecimal unrealizedProfitLossPercentage;

    @Schema(description = "Lucro ou prejuízo já realizado em vendas anteriores", example = "5000.00")
    private BigDecimal realizedProfitLoss;

    @Schema(description = "Percentual de alocação deste ativo no total da carteira", example = "45.50")
    private BigDecimal allocationPercentage;

    @Schema(description = "Total de transações executadas para este ativo", example = "4")
    private Integer transactionsCount;

    @Schema(description = "Variação percentual nas últimas 24 horas", example = "2.45")
    private BigDecimal change24hPercent;

    @Schema(description = "Moeda dos valores exibidos", example = "USD")
    private String currency;
}
