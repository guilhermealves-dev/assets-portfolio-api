package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetResponse {

    @Schema(description = "ID do ativo", example = "5f9b1b3b-8b1b-4b1b-8b1b-8b1b3b8b1b3b")
    private String id;

    @Schema(description = "Ticker ou símbolo do ativo", example = "BTC")
    private String symbol;

    @Schema(description = "Nome descritivo do ativo", example = "Bitcoin")
    private String name;

    @Schema(description = "Tipo do ativo", example = "CRYPTO")
    private AssetType type;

    @Schema(description = "URL do logotipo ou ícone do ativo", example = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png")
    private String imgUrl;
}
