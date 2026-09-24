package com.solaria.persistence.openapi.unit;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.unit.UnitSpecificationsRequestDTO;
import com.solaria.persistence.dto.response.unit.UnitSpecificationsResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Especificações de Unidade", description = "Gerenciamento das especificações técnicas de unidades locais (histórico imutável)")
public interface UnitSpecificationsOpenApi {

    @Operation(
        summary = "Cria um novo registro de especificação de unidade",
        description = "A data é sempre definida pelo servidor, nunca pelo cliente; o histórico é imutável (só criação e consulta)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Especificação de unidade criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<UnitSpecificationsResponseDTO> save(UnitSpecificationsRequestDTO dto);

    @Operation(summary = "Busca uma especificação de unidade pelo identificador e empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Especificação de unidade encontrada"),
            @ApiResponse(responseCode = "404", description = "Especificação de unidade não encontrada")
    })
    ResponseEntity<UnitSpecificationsResponseDTO> findById(UUID id, UUID companyId);

    @Operation(
        summary = "Lista o histórico de especificações de uma unidade local",
        description = "A listagem mostra o registro mais recente primeiro."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de especificações retornada com sucesso")
    })
    ResponseEntity<List<UnitSpecificationsResponseDTO>> findByLocalUnit(UUID localUnitId);
}
