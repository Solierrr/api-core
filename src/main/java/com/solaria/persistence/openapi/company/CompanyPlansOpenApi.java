package com.solaria.persistence.openapi.company;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.response.company.CompanyPlansResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Company Plans", description = "Gerenciamento dos planos de assinatura vinculados às empresas.")
public interface CompanyPlansOpenApi {

    @Operation(summary = "Busca um plano de empresa pelo identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plano encontrado"),
            @ApiResponse(responseCode = "404", description = "Plano não encontrado")
    })
    ResponseEntity<CompanyPlansResponseDTO> findById(UUID id);

    @Operation(summary = "Lista todos os planos de empresa cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso")
    })
    ResponseEntity<List<CompanyPlansResponseDTO>> findAll();
}
