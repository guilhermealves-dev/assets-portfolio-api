package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Item da Watchlist com dados cadastrais e cotação de mercado em tempo real")
public class WatchlistItemResponse {

    @Schema(description = "ID do item na watchlist", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "ID único do ativo", example = "3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f")
    private String assetId;

    @Schema(description = "Símbolo/Ticker do ativo", example = "BTC")
    private String symbol;

    @Schema(description = "Nome do ativo", example = "Bitcoin")
    private String name;

    @Schema(description = "Classe/Tipo do ativo", example = "CRYPTO")
    private AssetType type;

    @Schema(description = "URL do logotipo em alta definição do ativo")
    private String imgUrl;

    @Schema(description = "Preço de mercado atual", example = "65432.10")
    private BigDecimal currentPrice;

    @Schema(description = "Variação percentual nas últimas 24 horas", example = "2.45")
    private BigDecimal change24h;

    @Schema(description = "Preço máximo nas últimas 24 horas", example = "66100.00")
    private BigDecimal high24h;

    @Schema(description = "Preço mínimo nas últimas 24 horas", example = "64200.00")
    private BigDecimal low24h;

    @Schema(description = "Moeda da cotação", example = "USD")
    private String currency;

    @Schema(description = "Data/hora em que o ativo foi adicionado à watchlist")
    private LocalDateTime addedAt;
}
