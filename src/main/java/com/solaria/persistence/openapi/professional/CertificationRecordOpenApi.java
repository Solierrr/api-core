package com.solaria.persistence.openapi.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.professional.CertificationRecordRequestDTO;
import com.solaria.persistence.dto.response.professional.CertificationRecordResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Registros de Certificação", description = "Gerenciamento dos registros de certificação vinculados a profissionais")
public interface CertificationRecordOpenApi {

    @Operation(
        summary = "Cria um novo registro de certificação",
        description = "Exige registro profissional do próprio User provisionado, sem vínculo empresarial. ProfessionalRegistration obrigatório; Certification opcional; sem checagem de duplicidade do par."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Recurso, pai ou User provisionado não encontrado (USER_NOT_PROVISIONED)"),
            @ApiResponse(responseCode = "401", description = "JWT ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Recurso ou vínculo de outro titular, inclusive para ADMIN empresarial"),
            @ApiResponse(responseCode = "201", description = "Registro de certificação criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para criação do registro de certificação")
    })
    ResponseEntity<CertificationRecordResponseDTO> save(CertificationRecordRequestDTO dto);

    @Operation(
        summary = "Atualiza um registro de certificação existente",
        description = "Exige registro profissional do próprio User provisionado, sem vínculo empresarial. ProfessionalRegistration obrigatório; Certification opcional; sem checagem de duplicidade do par."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "JWT ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Recurso ou vínculo de outro titular, inclusive para ADMIN empresarial"),
            @ApiResponse(responseCode = "200", description = "Registro de certificação atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização do registro de certificação"),
            @ApiResponse(responseCode = "404", description = "Recurso, registro profissional ou User provisionado não encontrado (USER_NOT_PROVISIONED)")
    })
    ResponseEntity<CertificationRecordResponseDTO> update(UUID id, CertificationRecordRequestDTO dto);

    @Operation(summary = "Remove um registro de certificação pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "JWT ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Recurso ou vínculo de outro titular, inclusive para ADMIN empresarial"),
            @ApiResponse(responseCode = "204", description = "Registro de certificação removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Recurso, registro profissional ou User provisionado não encontrado (USER_NOT_PROVISIONED)")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca um registro de certificação pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "JWT ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Recurso ou vínculo de outro titular, inclusive para ADMIN empresarial"),
            @ApiResponse(responseCode = "200", description = "Registro de certificação encontrado"),
            @ApiResponse(responseCode = "404", description = "Recurso, registro profissional ou User provisionado não encontrado (USER_NOT_PROVISIONED)")
    })
    ResponseEntity<CertificationRecordResponseDTO> findById(UUID id);

    @Operation(summary = "Lista os registros de certificação de um registro profissional")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Recurso, pai ou User provisionado não encontrado (USER_NOT_PROVISIONED)"),
            @ApiResponse(responseCode = "401", description = "JWT ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Recurso ou vínculo de outro titular, inclusive para ADMIN empresarial"),
            @ApiResponse(responseCode = "200", description = "Lista de registros de certificação retornada com sucesso")
    })
    ResponseEntity<List<CertificationRecordResponseDTO>> findByProfessionalRegistration(UUID professionalRegistrationId);
}
