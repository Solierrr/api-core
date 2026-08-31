package com.solaria.persistence.openapi.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.identity.PermissionRequestDTO;
import com.solaria.persistence.dto.response.identity.PermissionResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Permissões", description = "Gerenciamento das permissões do sistema")
public interface PermissionOpenApi {

    @Operation(
        summary = "Cria uma nova permissão",
        description = "O nome da permissão deve ser único e ter no máximo 100 caracteres."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Permissão criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para criação da permissão"),
            @ApiResponse(responseCode = "409", description = "Já existe uma permissão com o mesmo nome")
    })
    ResponseEntity<PermissionResponseDTO> save(PermissionRequestDTO dto);

    @Operation(
        summary = "Atualiza uma permissão existente",
        description = "O nome da permissão deve ser único e ter no máximo 100 caracteres."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissão atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização da permissão"),
            @ApiResponse(responseCode = "404", description = "Permissão não encontrada")
    })
    ResponseEntity<PermissionResponseDTO> update(UUID id, PermissionRequestDTO dto);

    @Operation(
        summary = "Remove uma permissão pelo id",
        description = "A exclusão nunca é bloqueada: todos os vínculos de cargo com essa permissão são removidos automaticamente em cascata."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Permissão removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Permissão não encontrada")
    })
    ResponseEntity<Void> deleteById(UUID id);

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
