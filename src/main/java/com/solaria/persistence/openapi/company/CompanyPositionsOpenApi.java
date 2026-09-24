package com.solaria.persistence.openapi.company;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.company.CompanyPositionsRequestDTO;
import com.solaria.persistence.dto.response.company.CompanyPositionsResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Cargos da Empresa", description = "Gerenciamento dos vínculos entre empresas e cargos")
public interface CompanyPositionsOpenApi {

    @Operation(
        summary = "Cria um novo vínculo entre empresa e cargo",
        description = "A combinação empresa+cargo não pode se repetir."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vínculo empresa-cargo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para criação do vínculo"),
            @ApiResponse(responseCode = "409", description = "Vínculo entre a empresa e o cargo já existe")
    })
    ResponseEntity<CompanyPositionsResponseDTO> save(CompanyPositionsRequestDTO dto);

    @Operation(
        summary = "Remove um vínculo empresa-cargo pelo id",
        description = "Não é possível excluir se algum UserCompany usa essa combinação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vínculo empresa-cargo removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Vínculo empresa-cargo não encontrado")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca um vínculo empresa-cargo pelo id e pela empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vínculo empresa-cargo encontrado"),
            @ApiResponse(responseCode = "404", description = "Vínculo empresa-cargo não encontrado")
    })
    ResponseEntity<CompanyPositionsResponseDTO> findById(UUID id, UUID companyId);

    @Operation(summary = "Lista todos os cargos vinculados a uma empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de vínculos empresa-cargo retornada com sucesso")
    })
    ResponseEntity<List<CompanyPositionsResponseDTO>> findAllByCompany(UUID companyId);
}
