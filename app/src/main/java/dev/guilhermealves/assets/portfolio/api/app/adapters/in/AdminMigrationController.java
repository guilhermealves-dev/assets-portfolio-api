package dev.guilhermealves.assets.portfolio.api.app.adapters.in;

import dev.guilhermealves.assets.portfolio.api.app.domain.core.DataMigrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("admin")
@Tag(name = "Admin / Migrations", description = "Operações administrativas e migração de dados")
public class AdminMigrationController {

    private final DataMigrationService migrationService;

    @PostMapping("/migrate-data")
    @Operation(summary = "Migrar e normalizar dados legados no Firebase",
            description = "Normaliza os valores de type para UPPERCASE e preenche walletId/assetId nas transações antigas salvas antes da introdução dos Enums.")
    @ApiResponse(responseCode = "200", description = "Migração realizada com sucesso")
    public ResponseEntity<Map<String, Object>> migrate() {
        try {
            return ResponseEntity.ok(migrationService.migrateExistingData());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao executar migração de dados: " + e.getMessage(), e);
        }
    }
}
