package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetRequest {

    @Schema(description = "ID do ativo", example = "btc-uuid")
    private String id;

    @NotBlank(message = "O símbolo do ativo é obrigatório")
    @Schema(description = "Ticker ou símbolo do ativo", example = "BTC")
    private String symbol;

    @NotBlank(message = "O nome do ativo é obrigatório")
    @Schema(description = "Nome descritivo do ativo", example = "Bitcoin")
    private String name;

    @NotNull(message = "O tipo do ativo é obrigatório (CRYPTO, STOCK_US, STOCK_BR, ETF, etc.)")
    @Schema(description = "Tipo do ativo", example = "CRYPTO")
    private AssetType type;

    @Schema(description = "URL do logotipo ou ícone do ativo", example = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png")
    private String imgUrl;
}
