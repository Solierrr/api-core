package com.solaria.persistence.openapi.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.response.professional.ProfessionResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Profissões", description = "Gerenciamento das profissões cadastradas")
public interface ProfessionOpenApi {

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
