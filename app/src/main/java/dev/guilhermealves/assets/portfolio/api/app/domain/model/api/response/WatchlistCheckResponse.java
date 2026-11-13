package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Status de inclusão do ativo na Watchlist do usuário")
public class WatchlistCheckResponse {

    @Schema(description = "ID do ativo cadastrado", example = "3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f")
    private String assetId;

    @Schema(description = "Símbolo/Ticker do ativo", example = "BTC")
    private String symbol;

    @Schema(description = "Indica se o ativo está presente na Watchlist do usuário", example = "true")
    private boolean inWatchlist;
}
