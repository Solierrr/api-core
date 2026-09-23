package com.solaria.persistence.openapi.identity;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.response.identity.SelfProfileResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Perfil pessoal", description = "Perfil do usuário autenticado para o app do profissional")
public interface SelfProfileOpenApi {

    @Operation(summary = "Consulta o próprio perfil", description = "Resolve o usuário pelo sub do JWT, sem cargo "
            + "ou vínculo empresarial obrigatório. Conta incompleta retorna person/technician nulos e coleções vazias. "
            + "Inclui todas as afiliações ativas e memberships, sem primária, username, CPF, nascimento ou permissões. "
            + "Fotos PROFILE/BANNER prevalecem sobre users.avatar/banner; ausência retorna null.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil; id, authId e technician.id são UUIDs distintos"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "404", description = "USER_NOT_PROVISIONED no ProblemDetail existente")
    })
    ResponseEntity<SelfProfileResponseDTO> getProfile();
}
