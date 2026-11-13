package dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado do processamento da importação de transações via CSV")
public class TransactionImportResponse {

    @Schema(description = "Quantidade total de linhas processadas", example = "10")
    private int totalProcessed;

    @Schema(description = "Quantidade de transações importadas com sucesso", example = "9")
    private int totalSuccess;

    @Schema(description = "Quantidade de transações com falha na importação", example = "1")
    private int totalFailed;

    @Builder.Default
    @Schema(description = "Lista de transações registradas com sucesso")
    private List<TransactionResponse> importedTransactions = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Lista descritiva de erros encontrados durante a importação")
    private List<String> errors = new ArrayList<>();
}
