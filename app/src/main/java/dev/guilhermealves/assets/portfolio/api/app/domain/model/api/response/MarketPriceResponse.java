package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
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
public class MarketPriceResponse {

    @Schema(description = "Símbolo/Ticker do ativo", example = "BTC")
    private String symbol;

    @Schema(description = "Nome do ativo", example = "Bitcoin")
    private String name;

    @Schema(description = "Tipo do ativo", example = "CRYPTO")
    private AssetType type;

    @Schema(description = "Preço de mercado na moeda solicitada", example = "68250.00")
    private BigDecimal price;

    @Schema(description = "Preço de referência em USD", example = "68250.00")
    private BigDecimal priceInUsd;

    @Schema(description = "Preço de referência em BRL", example = "385612.50")
    private BigDecimal priceInBrl;

    @Schema(description = "Variação percentual nas últimas 24 horas", example = "2.45")
    private BigDecimal change24hPercent;

    @Schema(description = "Moeda da cotação", example = "USD")
    private String currency;

    @Schema(description = "Provedor da cotação em tempo real", example = "BINANCE")
    private String source;

    @Schema(description = "Data e hora da cotação")
    private LocalDateTime updatedAt;
}
