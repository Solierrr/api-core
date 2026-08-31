package com.solaria.persistence.openapi.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.professional.ProfessionRequestDTO;
import com.solaria.persistence.dto.response.professional.ProfessionResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Profissões", description = "Gerenciamento das profissões cadastradas")
public interface ProfessionOpenApi {

    @Operation(summary = "Cria uma nova profissão")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profissão criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para criação da profissão"),
            @ApiResponse(responseCode = "409", description = "Já existe uma profissão com o mesmo nome")
    })
    ResponseEntity<ProfessionResponseDTO> save(ProfessionRequestDTO dto);

    @Operation(summary = "Atualiza uma profissão existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profissão atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização da profissão"),
            @ApiResponse(responseCode = "404", description = "Profissão não encontrada")
    })
    ResponseEntity<ProfessionResponseDTO> update(UUID id, ProfessionRequestDTO dto);

    @Operation(
        summary = "Remove uma profissão pelo id",
        description = "Não é possível excluir uma profissão que possua ProfessionalRegistration vinculado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Profissão removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Profissão não encontrada")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca uma profissão pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profissão encontrada"),
            @ApiResponse(responseCode = "404", description = "Profissão não encontrada")
    })
    ResponseEntity<ProfessionResponseDTO> findById(UUID id);

    @Operation(summary = "Lista todas as profissões cadastradas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de profissões retornada com sucesso")
    })
    ResponseEntity<List<ProfessionResponseDTO>> findAll();
}
