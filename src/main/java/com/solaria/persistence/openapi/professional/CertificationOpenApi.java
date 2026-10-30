package com.solaria.persistence.openapi.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.response.professional.CertificationResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Certificações", description = "Gerenciamento do cadastro de certificações técnicas/profissionais.")
public interface CertificationOpenApi {

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
