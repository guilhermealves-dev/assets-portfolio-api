package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response;

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
public class ExchangeRateResponse {

    @Schema(description = "Moeda de origem", example = "USD")
    private String from;

    @Schema(description = "Moeda de destino", example = "BRL")
    private String to;

    @Schema(description = "Taxa de câmbio", example = "5.6527")
    private BigDecimal rate;

    @Schema(description = "Fonte da cotação cambial", example = "BINANCE_USDTBRL")
    private String source;

    @Schema(description = "Data e hora da cotação")
    private LocalDateTime updatedAt;
}
