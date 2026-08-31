package com.solaria.persistence.openapi.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.identity.PositionRequestDTO;
import com.solaria.persistence.dto.response.identity.PositionResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Cargos", description = "Gerenciamento dos cargos disponíveis na plataforma")
public interface PositionOpenApi {

    @Operation(summary = "Cria um novo cargo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cargo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<PositionResponseDTO> save(PositionRequestDTO dto);

    @Operation(summary = "Atualiza um cargo existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cargo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado")
    })
    ResponseEntity<PositionResponseDTO> update(UUID id, PositionRequestDTO dto);

    @Operation(
        summary = "Remove um cargo",
        description = "Não é permitida a exclusão de um cargo em uso (UserCompany/CompanyPositions); ao excluir um cargo sem uso, suas permissões são removidas automaticamente. O cargo ADMIN tem permissões protegidas contra revogação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cargo removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca um cargo pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cargo encontrado"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado")
    })
    ResponseEntity<PositionResponseDTO> findById(UUID id);

    @Operation(summary = "Lista todos os cargos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    })
    ResponseEntity<List<PositionResponseDTO>> findAll();
}
