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
public class AssetPerformanceResponse {

    @Schema(description = "ID do ativo", example = "uuid-1234")
    private String assetId;

    @Schema(description = "Ticker ou símbolo do ativo", example = "NVDA")
    private String symbol;

    @Schema(description = "Nome do ativo", example = "NVIDIA Corporation")
    private String name;

    @Schema(description = "Tipo do ativo", example = "STOCK_US")
    private AssetType type;

    @Schema(description = "URL do logotipo", example = "https://financialmodelingprep.com/image-stock/NVDA.png")
    private String imgUrl;

    @Schema(description = "Lucro ou prejuízo não realizado em valor monetário", example = "4500.00")
    private BigDecimal unrealizedProfitLoss;

    @Schema(description = "Rentabilidade percentual do ativo (%)", example = "35.50")
    private BigDecimal unrealizedProfitLossPercentage;

    @Schema(description = "Preço atual de mercado", example = "142.50")
    private BigDecimal currentPrice;
}
