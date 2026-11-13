package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortfolioChartResponse {

    @Schema(description = "Janela de tempo consultada (24H, 7D, 30D, 90D, 1Y, ALL)", example = "30D")
    private String range;

    @Schema(description = "Moeda dos valores exibidos", example = "USD")
    private String currency;

    @Schema(description = "Patrimônio atual ao final do período", example = "150000.00")
    private BigDecimal currentBalance;

    @Schema(description = "Capital total investido", example = "120000.00")
    private BigDecimal totalInvested;

    @Schema(description = "Variação patrimonial no período em valor financeiro ($)", example = "30000.00")
    private BigDecimal changeAmount;

    @Schema(description = "Variação patrimonial no período em percentual (%)", example = "25.00")
    private BigDecimal changePercentage;

    @Schema(description = "Série temporal de dados para renderização do gráfico")
    private List<ChartDataPointResponse> points;
}
