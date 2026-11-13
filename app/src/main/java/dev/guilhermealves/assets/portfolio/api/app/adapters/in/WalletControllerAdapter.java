package dev.guilhermealves.assets.portfolio.api.app.adapters.in;

import dev.guilhermealves.assets.portfolio.api.app.domain.core.PortfolioAnalyticsCore;
import dev.guilhermealves.assets.portfolio.api.app.domain.core.TransactionCore;
import dev.guilhermealves.assets.portfolio.api.app.domain.core.WalletCore;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.WalletRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.WalletSwapRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.WalletTransferRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.*;
import dev.guilhermealves.assets.portfolio.api.app.exception.ResourceNotFoundException;
import dev.guilhermealves.assets.portfolio.api.app.ports.in.ControllerIntegration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("wallets")
@Tag(name = "Wallets", description = "Endpoints para gerenciamento de carteiras/portfólios")
public class WalletControllerAdapter implements ControllerIntegration<WalletRequest, WalletResponse, String> {

    private final WalletCore core;
    private final PortfolioAnalyticsCore analyticsCore;
    private final TransactionCore transactionCore;

    @Override
    @PostMapping
    @Operation(summary = "Criar carteira", description = "Cria uma nova carteira para o usuário autenticado")
    @ApiResponse(responseCode = "201", description = "Carteira criada com sucesso")
    public ResponseEntity<WalletResponse> create(@RequestBody @Valid WalletRequest wallet) {
        try {
            return new ResponseEntity<>(core.create(wallet), HttpStatus.CREATED);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @GetMapping("/{id}")
    @Operation(summary = "Buscar carteira por ID", description = "Retorna os detalhes de uma carteira específica")
    @ApiResponse(responseCode = "200", description = "Carteira encontrada")
    @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    public ResponseEntity<WalletResponse> find(@PathVariable String id) {
        try {
            return core.findById(id)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("Carteira", id));
        } catch (ResourceNotFoundException | AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Resumo analítico da carteira",
            description = "Retorna o saldo total consolidado, total investido, lucros não realizados e lucros já realizados da carteira")
    @ApiResponse(responseCode = "200", description = "Resumo da carteira calculado com sucesso")
    @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    public ResponseEntity<WalletSummaryResponse> getSummary(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "USD") String currency) {
        try {
            return ResponseEntity.ok(analyticsCore.calculateWalletSummary(id, null, currency));
        } catch (ResourceNotFoundException | AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{id}/holdings")
    @Operation(summary = "Posições em custódia da carteira (Holdings)",
            description = "Retorna a lista de ativos mantidos na carteira com quantidade, preço médio ponderado, lucro/prejuízo e alocação percentual")
    @ApiResponse(responseCode = "200", description = "Lista de posições calculada com sucesso")
    @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    public ResponseEntity<List<HoldingResponse>> getHoldings(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "USD") String currency) {
        try {
            return ResponseEntity.ok(analyticsCore.calculateWalletHoldings(id, null, currency));
        } catch (ResourceNotFoundException | AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{id}/chart")
    @Operation(summary = "Gráfico e série histórica da carteira",
            description = "Retorna a série temporal de pontos (timestamp, saldo, investido, lucro) da carteira para os períodos 24H, 7D, 30D, 90D, 1Y ou ALL")
    @ApiResponse(responseCode = "200", description = "Série histórica da carteira calculada com sucesso")
    @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    public ResponseEntity<PortfolioChartResponse> getChart(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "30D") String range,
            @RequestParam(required = false, defaultValue = "USD") String currency) {
        try {
            return ResponseEntity.ok(analyticsCore.calculateWalletChart(id, range, currency));
        } catch (ResourceNotFoundException | AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @GetMapping
    @Operation(summary = "Listar carteiras", description = "Retorna todas as carteiras cadastradas")
    @ApiResponse(responseCode = "200", description = "Lista de carteiras retornada com sucesso")
    public ResponseEntity<List<WalletResponse>> list() {
        try {
            return ResponseEntity.ok(core.findAll());
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar carteira", description = "Atualiza os dados de uma carteira")
    @ApiResponse(responseCode = "200", description = "Carteira atualizada com sucesso")
    @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    public ResponseEntity<WalletResponse> update(@PathVariable String id, @RequestBody @Valid WalletRequest wallet) {
        try {
            wallet.setId(id);
            return ResponseEntity.ok(core.update(wallet));
        } catch (ResourceNotFoundException | AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar carteira", description = "Remove uma carteira pelo ID")
    @ApiResponse(responseCode = "204", description = "Carteira removida com sucesso")
    @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            core.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException | AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transferir ativo entre carteiras", description = "Transfere uma quantidade de determinado ativo entre duas carteiras pertencentes ao mesmo usuário")
    @ApiResponse(responseCode = "200", description = "Transferência realizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Saldo insuficiente ou dados inválidos")
    @ApiResponse(responseCode = "403", description = "Uma das carteiras não pertence ao usuário autenticado")
    public ResponseEntity<WalletTransferResponse> transfer(@RequestBody @Valid WalletTransferRequest request) {
        try {
            return ResponseEntity.ok(transactionCore.transferBetweenWallets(request));
        } catch (ResourceNotFoundException | AccessDeniedException | dev.guilhermealves.assets.portfolio.api.app.exception.BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/{walletId}/swap")
    @Operation(summary = "Troca direta de ativos (SWAP)", description = "Executa uma troca direta entre dois ativos dentro de uma carteira do usuário")
    @ApiResponse(responseCode = "200", description = "Swap realizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Saldo insuficiente do ativo de origem ou dados inválidos")
    @ApiResponse(responseCode = "403", description = "Carteira não pertence ao usuário autenticado")
    public ResponseEntity<WalletSwapResponse> swap(
            @PathVariable String walletId,
            @RequestBody @Valid WalletSwapRequest request) {
        try {
            return ResponseEntity.ok(transactionCore.swapInWallet(walletId, request));
        } catch (ResourceNotFoundException | AccessDeniedException | dev.guilhermealves.assets.portfolio.api.app.exception.BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(value = "/{walletId}/transactions/export", produces = "text/csv")
    @Operation(summary = "Exportar transações da carteira em CSV", description = "Gera e exporta arquivo CSV com o histórico de transações desta carteira")
    @ApiResponse(responseCode = "200", description = "CSV gerado com sucesso")
    public ResponseEntity<byte[]> exportWalletTransactions(@PathVariable String walletId) {
        try {
            byte[] csvBytes = transactionCore.exportTransactionsToCsv(walletId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"wallet_" + walletId + "_transactions.csv\"")
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(csvBytes);
        } catch (ResourceNotFoundException | AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(value = "/{walletId}/transactions/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar transações para a carteira via CSV", description = "Importa lote de transações diretamente para esta carteira")
    @ApiResponse(responseCode = "200", description = "Processamento concluído com resumo")
    public ResponseEntity<TransactionImportResponse> importWalletTransactions(
            @PathVariable String walletId,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("O arquivo CSV não pode estar vazio");
            }
            return ResponseEntity.ok(transactionCore.importTransactionsFromCsv(file.getInputStream(), walletId));
        } catch (ResourceNotFoundException | AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
