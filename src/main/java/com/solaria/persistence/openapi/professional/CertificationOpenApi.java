package com.solaria.persistence.openapi.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.professional.CertificationRequestDTO;
import com.solaria.persistence.dto.response.professional.CertificationResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Certifications", description = "Gerenciamento do cadastro de certificações técnicas/profissionais.")
public interface CertificationOpenApi {

    @Operation(summary = "Cadastra uma nova certificação")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Certificação criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<CertificationResponseDTO> save(CertificationRequestDTO dto);

    @Operation(summary = "Atualiza os dados de uma certificação existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Certificação atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Certificação não encontrada")
    })
    ResponseEntity<CertificationResponseDTO> update(UUID id, CertificationRequestDTO dto);

    @Operation(
        summary = "Remove uma certificação pelo identificador",
        description = "Não é possível excluir uma certificação que tenha CertificationRecord vinculado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Certificação removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Certificação não encontrada")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca uma certificação pelo identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Certificação encontrada"),
            @ApiResponse(responseCode = "404", description = "Certificação não encontrada")
    })
    ResponseEntity<CertificationResponseDTO> findById(UUID id);

    @Operation(summary = "Lista todas as certificações cadastradas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso")
    })
    ResponseEntity<List<CertificationResponseDTO>> findAll();
}
