package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistRequest {

    @Schema(description = "ID do ativo cadastrado no sistema (opcional se assetSymbol for informado)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String assetId;

    @Schema(description = "Símbolo/Ticker do ativo para busca ou criação automática", example = "BTC")
    private String assetSymbol;

    @Schema(description = "Classe/Tipo do ativo caso seja necessária criação automática", example = "CRYPTO")
    private AssetType assetType;

    @Schema(description = "Nome do ativo caso seja necessária criação automática", example = "Bitcoin")
    private String assetName;

    @JsonIgnore
    public String getEffectiveAssetIdentifier() {
        if (assetId != null && !assetId.isBlank()) {
            return assetId.trim();
        }
        if (assetSymbol != null && !assetSymbol.isBlank()) {
            return assetSymbol.trim().toUpperCase();
        }
        return null;
    }

    @AssertTrue(message = "O ID do ativo (assetId) ou o símbolo (assetSymbol) deve ser informado")
    @JsonIgnore
    public boolean isAssetSpecified() {
        return (assetId != null && !assetId.isBlank()) || (assetSymbol != null && !assetSymbol.isBlank());
    }
}
