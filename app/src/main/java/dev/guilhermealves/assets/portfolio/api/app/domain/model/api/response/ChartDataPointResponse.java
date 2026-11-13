package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChartDataPointResponse {

    @Schema(description = "Data e hora do ponto no gráfico", example = "2026-11-12T14:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "Patrimônio total consolidado no momento", example = "125400.00")
    private BigDecimal balance;

    @Schema(description = "Custo financeiro total investido no momento", example = "100000.00")
    private BigDecimal invested;

    @Schema(description = "Lucro ou prejuízo acumulado no momento", example = "25400.00")
    private BigDecimal profitLoss;
}
