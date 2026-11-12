package dev.guilhermealves.assets.portfolio.api.app.adapters.in;

import dev.guilhermealves.assets.portfolio.api.app.domain.core.UserCore;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.UserRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.UserResponse;
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
@RequestMapping("users")
@Tag(name = "Users", description = "Endpoints para gerenciamento de usuários")
public class UserControllerAdapter implements ControllerIntegration<UserRequest, UserResponse, String> {

    private final UserCore core;
    private final dev.guilhermealves.assets.portfolio.api.app.domain.core.SecurityCore securityCore;

    @GetMapping("/me")
    @Operation(summary = "Obter dados do usuário autenticado", description = "Retorna o perfil do usuário atualmente autenticado via token JWT")
    @ApiResponse(responseCode = "200", description = "Dados do usuário autenticado")
    public ResponseEntity<UserResponse> getAuthenticatedUser() {
        try {
            return ResponseEntity.ok(securityCore.getCurrentUser());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @PostMapping
    @Operation(summary = "Criar usuário", description = "Registra um novo usuário na plataforma")
    @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso")
    public ResponseEntity<UserResponse> create(@RequestBody @Valid UserRequest user) {
        try {
            return new ResponseEntity<>(core.create(user), HttpStatus.CREATED);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna os dados cadastrais de um usuário")
    @ApiResponse(responseCode = "200", description = "Usuário encontrado")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    public ResponseEntity<UserResponse> find(@PathVariable String id) {
        try {
            return core.findById(id)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @GetMapping
    @Operation(summary = "Listar usuários", description = "Retorna todos os usuários cadastrados")
    @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso")
    public ResponseEntity<List<UserResponse>> list() {
        try {
            return ResponseEntity.ok(core.findAll());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário")
    @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    public ResponseEntity<UserResponse> update(@PathVariable String id, @RequestBody @Valid UserRequest user) {
        try {
            user.setId(id);
            return ResponseEntity.ok(core.update(user));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar usuário", description = "Remove um usuário pelo ID")
    @ApiResponse(responseCode = "204", description = "Usuário removido com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
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
}
