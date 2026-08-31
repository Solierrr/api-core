package com.solaria.persistence.openapi.execution;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.execution.RequesterRequestDTO;
import com.solaria.persistence.dto.response.execution.RequesterResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Solicitantes", description = "Gerenciamento dos solicitantes vinculados às empresas")
public interface RequesterOpenApi {

    @Operation(
        summary = "Cria um novo solicitante",
        description = "Só pode ser criado para uma Company já APPROVED; mutuamente exclusivo com o papel de Supplier; no máximo um solicitante por empresa. Imutável após a criação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Solicitante criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<RequesterResponseDTO> save(RequesterRequestDTO dto);

    @Operation(
        summary = "Remove um solicitante",
        description = "Não é possível excluir um solicitante que possua Proposal ou TechnicalProject vinculado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Solicitante removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Solicitante não encontrado")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca um solicitante pelo id e pela empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitante encontrado"),
            @ApiResponse(responseCode = "404", description = "Solicitante não encontrado")
    })
    ResponseEntity<RequesterResponseDTO> findById(UUID id, UUID companyId);

    @Operation(summary = "Lista os solicitantes de uma empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    })
    ResponseEntity<List<RequesterResponseDTO>> findByCompany(UUID companyId);
}
