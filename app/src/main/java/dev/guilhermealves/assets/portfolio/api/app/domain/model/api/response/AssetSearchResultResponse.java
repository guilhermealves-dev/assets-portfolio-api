package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetSearchResultResponse {

    @Schema(description = "Símbolo/Ticker do ativo", example = "BTC")
    private String symbol;

    @Schema(description = "Nome descritivo", example = "Bitcoin")
    private String name;

    @Schema(description = "Tipo do ativo", example = "CRYPTO")
    private AssetType type;

    @Schema(description = "Preço de mercado atual", example = "68250.00")
    private BigDecimal currentPrice;

    @Schema(description = "Moeda do preço", example = "USD")
    private String currency;

    @Schema(description = "URL do ícone oficial do ativo", example = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png")
    private String imgUrl;

    @Schema(description = "Origem do ativo (LOCAL ou EXTERNAL)", example = "LOCAL")
    private String origin;
}
