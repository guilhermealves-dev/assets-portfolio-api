package dev.guilhermealves.assets.portfolio.api.app.adapters.in;

import dev.guilhermealves.assets.portfolio.api.app.domain.core.WatchlistCore;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.WatchlistRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.WatchlistCheckResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.WatchlistItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("watchlist")
@Tag(name = "Watchlist", description = "Endpoints para gerenciamento de ativos favoritos do usuário com cotações em tempo real")
public class WatchlistControllerAdapter {

    private final WatchlistCore watchlistCore;

    @PostMapping
    @Operation(summary = "Adicionar ativo à Watchlist", description = "Adiciona um ativo aos favoritos do usuário autenticado informando assetId ou assetSymbol")
    @ApiResponse(responseCode = "201", description = "Ativo adicionado à watchlist com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos")
    public ResponseEntity<WatchlistItemResponse> addToWatchlist(
            @RequestBody @Valid WatchlistRequest request,
            @Parameter(description = "Moeda para cotação (USD ou BRL)", example = "USD")
            @RequestParam(required = false, defaultValue = "USD") String currency) {
        try {
            return new ResponseEntity<>(watchlistCore.addToWatchlist(request, currency), HttpStatus.CREATED);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping
    @Operation(summary = "Listar ativos da Watchlist", description = "Retorna todos os ativos favoritados pelo usuário autenticado enriquecidos com cotação atual e variação 24h")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<WatchlistItemResponse>> getWatchlist(
            @Parameter(description = "Moeda para exibição das cotações (USD ou BRL)", example = "USD")
            @RequestParam(required = false, defaultValue = "USD") String currency) {
        try {
            return ResponseEntity.ok(watchlistCore.getWatchlist(currency));
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/{assetIdentifier}")
    @Operation(summary = "Remover ativo da Watchlist", description = "Remove um ativo da lista de favoritos informando o ID do ativo ou seu símbolo (ex: BTC, PETR4)")
    @ApiResponse(responseCode = "204", description = "Ativo removido com sucesso")
    public ResponseEntity<?> removeFromWatchlist(
            @Parameter(description = "ID do ativo ou símbolo/ticker", example = "BTC")
            @PathVariable String assetIdentifier) {
        try {
            watchlistCore.removeFromWatchlist(assetIdentifier);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/check/{assetIdentifier}")
    @Operation(summary = "Verificar se ativo está na Watchlist", description = "Verifica se um ativo específico está favoritado pelo usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso")
    public ResponseEntity<WatchlistCheckResponse> checkWatchlist(
            @Parameter(description = "ID do ativo ou símbolo/ticker", example = "BTC")
            @PathVariable String assetIdentifier) {
        try {
            return ResponseEntity.ok(watchlistCore.checkWatchlist(assetIdentifier));
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
