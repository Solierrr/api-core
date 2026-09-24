package com.solaria.persistence.openapi.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.professional.ShiftRequestDTO;
import com.solaria.persistence.dto.response.professional.ShiftResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Turnos", description = "Gerenciamento dos turnos de trabalho dos técnicos")
public interface ShiftOpenApi {

    @Operation(
        summary = "Cria um novo turno",
        description = "Técnico, dia da semana, início e fim são obrigatórios; o fim precisa ser estritamente posterior ao início."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Turno criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Técnico não encontrado")
    })
    ResponseEntity<ShiftResponseDTO> save(ShiftRequestDTO dto);

    @Operation(
        summary = "Atualiza um turno existente",
        description = "O fim precisa continuar estritamente posterior ao início."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turno atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Turno ou técnico não encontrado")
    })
    ResponseEntity<ShiftResponseDTO> update(UUID id, ShiftRequestDTO dto);

    @Operation(summary = "Remove um turno")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Turno removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Turno não encontrado")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca um turno pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turno encontrado"),
            @ApiResponse(responseCode = "404", description = "Turno não encontrado")
    })
    ResponseEntity<ShiftResponseDTO> findById(UUID id);

    @Operation(summary = "Lista os turnos de um técnico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de turnos retornada com sucesso")
    })
    ResponseEntity<List<ShiftResponseDTO>> findByTechnician(UUID technicianId);
}
