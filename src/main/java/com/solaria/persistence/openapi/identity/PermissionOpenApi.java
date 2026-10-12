package com.solaria.persistence.openapi.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.response.identity.PermissionResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Permissões", description = "Gerenciamento das permissões do sistema")
public interface PermissionOpenApi {

    @Operation(summary = "Busca uma permissão pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissão encontrada"),
            @ApiResponse(responseCode = "404", description = "Permissão não encontrada")
    })
    ResponseEntity<PermissionResponseDTO> findById(UUID id);

    @Operation(summary = "Lista todas as permissões cadastradas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de permissões retornada com sucesso")
    })
    ResponseEntity<List<PermissionResponseDTO>> findAll();
}
