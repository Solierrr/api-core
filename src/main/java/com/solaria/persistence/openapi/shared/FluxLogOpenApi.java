package com.solaria.persistence.openapi.shared;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.shared.FluxLogRequestDTO;
import com.solaria.persistence.dto.response.shared.FluxLogResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Log de Atividade", description = "Registro da última ação de cada usuário no sistema")
public interface FluxLogOpenApi {

    @Operation(
        summary = "Registra uma nova ação de usuário",
        description = "Registro append-only do próprio usuário autenticado, sem alterar estado da conta."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "201", description = "Ação registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Recurso de outro titular"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    ResponseEntity<FluxLogResponseDTO> record(FluxLogRequestDTO dto);

    @Operation(summary = "Lista o histórico de ações de um usuário, mais recente primeiro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico próprio retornado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Histórico de outro titular"),
            @ApiResponse(responseCode = "404", description = "Usuário autenticado não provisionado")
    })
    ResponseEntity<List<FluxLogResponseDTO>> findByUser(UUID userId);
}
