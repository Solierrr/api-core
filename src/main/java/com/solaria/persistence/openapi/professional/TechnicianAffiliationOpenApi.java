package com.solaria.persistence.openapi.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.patch.professional.UpdateActiveDTO;
import com.solaria.persistence.dto.patch.professional.UpdateTypeDTO;
import com.solaria.persistence.dto.request.professional.TechnicianAffiliationRequestDTO;
import com.solaria.persistence.dto.response.professional.TechnicianAffiliationResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Afiliações de Técnicos", description = "Gerenciamento das afiliações de técnicos a entidades profissionais")
public interface TechnicianAffiliationOpenApi {

    @Operation(
        summary = "Cria uma nova afiliação de técnico",
        description = "Técnico é obrigatório; empresa é opcional (ausência indica técnico independente); só é permitido um vínculo ativo por técnico por vez."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Afiliação de técnico criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para criação da afiliação de técnico"),
            @ApiResponse(responseCode = "409", description = "Técnico já possui vínculo com uma empresa (permitido apenas um) ou já possui vínculo independente")
    })
    ResponseEntity<TechnicianAffiliationResponseDTO> save(TechnicianAffiliationRequestDTO dto);

    @Operation(
        summary = "Atualiza o tipo de uma afiliação de técnico",
        description = "O tipo de vínculo pode ser trocado a qualquer momento."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipo da afiliação de técnico atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização do tipo de afiliação"),
            @ApiResponse(responseCode = "404", description = "Afiliação de técnico não encontrada")
    })
    ResponseEntity<TechnicianAffiliationResponseDTO> updateType(UUID id, UpdateTypeDTO dto);

    @Operation(summary = "Ativa ou desativa uma afiliação de técnico",
            description = "Registros de afiliação nunca são apagados (histórico). Desativar (active=false) libera o técnico para criar um novo vínculo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Situação da afiliação de técnico atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização da situação"),
            @ApiResponse(responseCode = "404", description = "Afiliação de técnico não encontrada")
    })
    ResponseEntity<TechnicianAffiliationResponseDTO> updateActive(UUID id, UpdateActiveDTO dto);

    @Operation(summary = "Busca uma afiliação de técnico pelo id e pela empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Afiliação de técnico encontrada"),
            @ApiResponse(responseCode = "404", description = "Afiliação de técnico não encontrada")
    })
    ResponseEntity<TechnicianAffiliationResponseDTO> findById(UUID id, UUID companyId);

    @Operation(summary = "Lista todas as afiliações de técnicos de uma empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de afiliações de técnicos retornada com sucesso")
    })
    ResponseEntity<List<TechnicianAffiliationResponseDTO>> findAllByCompany(UUID companyId);

    @Operation(summary = "Lista as afiliações de um técnico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de afiliações do técnico retornada com sucesso")
    })
    ResponseEntity<List<TechnicianAffiliationResponseDTO>> findByTechnician(UUID technicianId);
}
