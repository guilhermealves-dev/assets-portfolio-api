package dev.guilhermealves.assets.portfolio.api.app.adapters.in;

import dev.guilhermealves.assets.portfolio.api.app.domain.core.PortfolioAnalyticsCore;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.HoldingResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.PortfolioChartResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.PortfolioSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("portfolio")
@Tag(name = "Portfolio Analytics", description = "Endpoints de consolidação global de patrimônio (estilo CoinMarketCap)")
public class PortfolioControllerAdapter {

    private final PortfolioAnalyticsCore analyticsCore;

    @GetMapping("/summary")
    @Operation(summary = "Resumo global do portfólio",
            description = "Consolida todas as carteiras do usuário, fornecendo saldo total, capital total investido, lucros abertos e lucros realizados")
    @ApiResponse(responseCode = "200", description = "Resumo do portfólio consolidado com sucesso")
    public ResponseEntity<PortfolioSummaryResponse> getPortfolioSummary(
            @Parameter(description = "ID do usuário para filtrar (opcional)") @RequestParam(required = false) String userId,
            @Parameter(description = "Moeda de exibição (USD ou BRL, padrão: USD)") @RequestParam(required = false, defaultValue = "USD") String currency) {
        try {
            return ResponseEntity.ok(analyticsCore.calculatePortfolioSummary(userId, null, currency));
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular resumo do portfólio: " + e.getMessage(), e);
        }
    }

    @GetMapping("/holdings")
    @Operation(summary = "Posições consolidadas do portfólio (Global Holdings)",
            description = "Retorna a lista de todos os ativos sob custódia agregando múltiplas carteiras, com preço médio ponderado global e alocação percentual")
    @ApiResponse(responseCode = "200", description = "Holdings consolidados retornados com sucesso")
    public ResponseEntity<List<HoldingResponse>> getPortfolioHoldings(
            @Parameter(description = "ID do usuário para filtrar (opcional)") @RequestParam(required = false) String userId,
            @Parameter(description = "Moeda de exibição (USD ou BRL, padrão: USD)") @RequestParam(required = false, defaultValue = "USD") String currency) {
        try {
            return ResponseEntity.ok(analyticsCore.calculatePortfolioHoldings(userId, null, currency));
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular holdings do portfólio: " + e.getMessage(), e);
        }
    }

    @GetMapping("/chart")
    @Operation(summary = "Gráfico e série histórica do portfólio consolidado",
            description = "Retorna a série temporal de pontos (timestamp, saldo, investido, lucro) somando todas as carteiras do usuário para os períodos 24H, 7D, 30D, 90D, 1Y ou ALL")
    @ApiResponse(responseCode = "200", description = "Série histórica do portfólio calculada com sucesso")
    public ResponseEntity<PortfolioChartResponse> getPortfolioChart(
            @Parameter(description = "ID do usuário para filtrar (opcional)") @RequestParam(required = false) String userId,
            @Parameter(description = "Janela temporal (24H, 7D, 30D, 90D, 1Y, ALL)") @RequestParam(required = false, defaultValue = "30D") String range,
            @Parameter(description = "Moeda de exibição (USD ou BRL, padrão: USD)") @RequestParam(required = false, defaultValue = "USD") String currency) {
        try {
            return ResponseEntity.ok(analyticsCore.calculatePortfolioChart(userId, range, currency));
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular gráfico do portfólio: " + e.getMessage(), e);
        }
    }
}
