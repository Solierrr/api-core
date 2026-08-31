package com.solaria.persistence.openapi.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.identity.PositionPermissionRequestDTO;
import com.solaria.persistence.dto.response.identity.PositionPermissionResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Position Permissions", description = "Gerenciamento das permissões concedidas a cargos (positions).")
public interface PositionPermissionOpenApi {

    @Operation(
        summary = "Concede uma permissão a um cargo",
        description = "A mesma permissão não pode ser concedida duas vezes ao mesmo cargo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Permissão concedida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Permissão já concedida a este cargo")
    })
    ResponseEntity<PositionPermissionResponseDTO> grant(PositionPermissionRequestDTO dto);

    @Operation(
        summary = "Revoga uma permissão concedida a um cargo",
        description = "Não é permitido revogar uma permissão do cargo ADMIN (403)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Permissão revogada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Permissão de cargo não encontrada")
    })
    ResponseEntity<Void> revoke(UUID id);

    @Operation(summary = "Lista as permissões concedidas a um cargo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso")
    })
    ResponseEntity<List<PositionPermissionResponseDTO>> findByPosition(UUID positionId);
}
