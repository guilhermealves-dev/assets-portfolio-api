package dev.guilhermealves.assets.portfolio.api.app.adapters.in;

import dev.guilhermealves.assets.portfolio.api.app.domain.core.AssetCore;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.AssetRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.AssetResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("assets")
@Tag(name = "Assets", description = "Endpoints para gerenciamento de ativos financeiros negociáveis")
public class AssetControllerAdapter implements ControllerIntegration<AssetRequest, AssetResponse, String> {

    private final AssetCore core;
    private final dev.guilhermealves.assets.portfolio.api.app.domain.core.MarketDataService marketDataService;

    @Override
    @PostMapping
    @Operation(summary = "Cadastrar novo ativo", description = "Cria um novo ativo disponível para negociação (criptomoeda, ação, etc.)")
    @ApiResponse(responseCode = "201", description = "Ativo cadastrado com sucesso")
    public ResponseEntity<AssetResponse> create(@RequestBody @Valid AssetRequest asset) {
        try {
            return new ResponseEntity<>(core.create(asset), HttpStatus.CREATED);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @GetMapping("/{id}")
    @Operation(summary = "Buscar ativo por ID ou Símbolo", description = "Retorna os detalhes de um ativo específico pelo ID ou ticker (ex: NVDA, BTC)")
    @ApiResponse(responseCode = "200", description = "Ativo encontrado")
    @ApiResponse(responseCode = "404", description = "Ativo não encontrado")
    public ResponseEntity<AssetResponse> find(@PathVariable String id) {
        try {
            return core.findByIdOrSymbol(id)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("Ativo", id));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @GetMapping
    @Operation(summary = "Listar ativos", description = "Retorna todos os ativos cadastrados")
    @ApiResponse(responseCode = "200", description = "Lista de ativos retornada com sucesso")
    public ResponseEntity<List<AssetResponse>> list() {
        try {
            return ResponseEntity.ok(core.findAll());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @RequestMapping(value = "/{id}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @Operation(summary = "Atualizar ativo", description = "Atualiza os dados de um ativo existente")
    @ApiResponse(responseCode = "200", description = "Ativo atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Ativo não encontrado")
    public ResponseEntity<AssetResponse> update(@PathVariable String id, @RequestBody @Valid AssetRequest asset) {
        try {
            asset.setId(id);
            return ResponseEntity.ok(core.update(asset));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar ativo", description = "Remove um ativo pelo ID")
    @ApiResponse(responseCode = "204", description = "Ativo removido com sucesso")
    @ApiResponse(responseCode = "404", description = "Ativo não encontrado")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            core.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar ativos para cadastro", description = "Pesquisa ativos locais e do mercado (Binance, Brapi) por nome ou símbolo")
    public ResponseEntity<List<dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.AssetSearchResultResponse>> search(
            @RequestParam String query) {
        return ResponseEntity.ok(marketDataService.searchAssets(query));
    }
}
