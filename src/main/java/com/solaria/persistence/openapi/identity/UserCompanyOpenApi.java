package com.solaria.persistence.openapi.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.patch.identity.UpdatePositionDTO;
import com.solaria.persistence.dto.request.identity.UserCompanyRequestDTO;
import com.solaria.persistence.dto.response.identity.UserCompanyResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User Companies", description = "Gerenciamento do vínculo entre usuários e empresas, incluindo o cargo (position) associado.")
public interface UserCompanyOpenApi {

    @Operation(
        summary = "Vincula um usuário a uma empresa",
        description = "Um usuário só pode ter um vínculo de empresa por vez (regra de aplicação); o cargo escolhido precisa estar habilitado para aquela empresa via CompanyPositions."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vínculo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Usuário já possui vínculo com uma empresa (permitido apenas um por usuário)")
    })
    ResponseEntity<UserCompanyResponseDTO> save(UserCompanyRequestDTO dto);

    @Operation(
        summary = "Atualiza o cargo (position) associado ao vínculo usuário-empresa",
        description = "O cargo escolhido precisa estar habilitado para a empresa do vínculo via CompanyPositions."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cargo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Vínculo usuário-empresa não encontrado")
    })
    ResponseEntity<UserCompanyResponseDTO> updatePosition(UUID id, UpdatePositionDTO dto);

    @Operation(summary = "Remove o vínculo entre um usuário e uma empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vínculo removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Vínculo usuário-empresa não encontrado")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca um vínculo usuário-empresa pelo identificador, escopado à empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vínculo encontrado"),
            @ApiResponse(responseCode = "404", description = "Vínculo usuário-empresa não encontrado")
    })
    ResponseEntity<UserCompanyResponseDTO> findById(UUID id, UUID companyId);

    @Operation(summary = "Lista os vínculos de usuários de uma empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso")
    })
    ResponseEntity<List<UserCompanyResponseDTO>> findAllByCompany(UUID companyId);

    @Operation(summary = "Lista os vínculos de empresas de um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso")
    })
    ResponseEntity<List<UserCompanyResponseDTO>> findByUser(UUID userId);
}
