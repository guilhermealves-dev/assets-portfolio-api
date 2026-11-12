package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionResponse {

    @Schema(description = "ID da transação", example = "tx-12345")
    private String id;

    @Schema(description = "Tipo da transação", example = "BUY")
    private TransactionType type;

    @Schema(description = "Dados do ativo")
    private AssetResponse asset;

    @Schema(description = "Dados da carteira")
    private WalletResponse wallet;

    @Schema(description = "Preço unitário", example = "60000.00")
    private BigDecimal price;

    @Schema(description = "Valor total financeiro", example = "15000.00")
    private BigDecimal amount;

    @Schema(description = "Quantidade negociada", example = "0.25")
    private BigDecimal quantity;

    @Schema(description = "Taxa paga", example = "2.50")
    private BigDecimal fee;

    @Schema(description = "Data e hora da transação", example = "2026-11-12T13:00:00")
    private LocalDateTime datetime;
}
