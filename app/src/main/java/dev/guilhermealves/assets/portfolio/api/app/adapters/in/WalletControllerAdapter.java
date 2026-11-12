package dev.guilhermealves.assets.portfolio.api.app.adapters.in;

import dev.guilhermealves.assets.portfolio.api.app.domain.core.PortfolioAnalyticsCore;
import dev.guilhermealves.assets.portfolio.api.app.domain.core.WalletCore;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.WalletRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.HoldingResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.WalletResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.WalletSummaryResponse;
import dev.guilhermealves.assets.portfolio.api.app.exception.ResourceNotFoundException;
import dev.guilhermealves.assets.portfolio.api.app.ports.in.ControllerIntegration;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("wallets")
@Tag(name = "Wallets", description = "Endpoints para gerenciamento de carteiras/portfólios")
public class WalletControllerAdapter implements ControllerIntegration<WalletRequest, WalletResponse, String> {

    private final WalletCore core;
    private final PortfolioAnalyticsCore analyticsCore;

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
}
