package dev.guilhermealves.assets.portfolio.api.app.adapters.in;

import dev.guilhermealves.assets.portfolio.api.app.domain.core.TransactionCore;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.TransactionRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.TransactionResponse;
import dev.guilhermealves.assets.portfolio.api.app.exception.ResourceNotFoundException;
import dev.guilhermealves.assets.portfolio.api.app.ports.in.ControllerIntegration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("transactions")
@Tag(name = "Transactions", description = "Endpoints para gerenciamento de transações (BUY, SELL, etc.)")
public class TransactionControllerAdapter implements ControllerIntegration<TransactionRequest, TransactionResponse, String> {

    private final TransactionCore core;

    @Override
    @PostMapping
    @Operation(summary = "Registrar transação", description = "Cadastra uma nova transação financeira (compra, venda, transferência, etc.)")
    @ApiResponse(responseCode = "201", description = "Transação registrada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados da transação inválidos")
    @ApiResponse(responseCode = "404", description = "Carteira ou Ativo não encontrado")
    public ResponseEntity<TransactionResponse> create(@RequestBody @Valid TransactionRequest transaction) {
        try {
            return new ResponseEntity<>(core.create(transaction), HttpStatus.CREATED);
        } catch (ResourceNotFoundException | AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @GetMapping("/{id}")
    @Operation(summary = "Buscar transação por ID", description = "Retorna os detalhes de uma transação específica")
    @ApiResponse(responseCode = "200", description = "Transação encontrada")
    @ApiResponse(responseCode = "404", description = "Transação não encontrada")
    public ResponseEntity<TransactionResponse> find(@PathVariable String id) {
        try {
            return core.findById(id)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("Transação", id));
        } catch (ResourceNotFoundException | AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<List<TransactionResponse>> list() {
        return listWithFilter(null, null, null, null);
    }

    @GetMapping
    @Operation(summary = "Listar transações com filtros e paginação", description = "Retorna transações podendo filtrar por carteira, ativo e paginar os resultados")
    @ApiResponse(responseCode = "200", description = "Lista de transações retornada com sucesso")
    public ResponseEntity<List<TransactionResponse>> listWithFilter(
            @Parameter(description = "ID da carteira para filtrar") @RequestParam(required = false) String walletId,
            @Parameter(description = "ID do ativo para filtrar") @RequestParam(required = false) String assetId,
            @Parameter(description = "Número da página (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Tamanho da página") @RequestParam(required = false) Integer size) {
        try {
            if (walletId != null || assetId != null || (size != null && size > 0)) {
                return ResponseEntity.ok(core.findByFilter(walletId, assetId, page, size));
            }
            return ResponseEntity.ok(core.findAll());
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar transação", description = "Atualiza os dados de uma transação existente")
    @ApiResponse(responseCode = "200", description = "Transação atualizada com sucesso")
    @ApiResponse(responseCode = "404", description = "Transação não encontrada")
    public ResponseEntity<TransactionResponse> update(@PathVariable String id, @RequestBody @Valid TransactionRequest transaction) {
        try {
            transaction.setId(id);
            return ResponseEntity.ok(core.update(transaction));
        } catch (ResourceNotFoundException | AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar transação", description = "Remove uma transação pelo ID")
    @ApiResponse(responseCode = "204", description = "Transação removida com sucesso")
    @ApiResponse(responseCode = "404", description = "Transação não encontrada")
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
}
