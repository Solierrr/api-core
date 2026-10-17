package com.solaria.persistence.openapi.company;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.response.company.BusinessContactResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Contatos Comerciais", description = "Gerenciamento dos contatos comerciais das empresas")
public interface BusinessContactOpenApi {

    @Operation(summary = "Busca um contato comercial pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contato comercial encontrado"),
            @ApiResponse(responseCode = "404", description = "Contato comercial não encontrado")
    })
    ResponseEntity<BusinessContactResponseDTO> findById(UUID id);

    @Operation(summary = "Lista o contato comercial da empresa atual")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    })
    ResponseEntity<List<BusinessContactResponseDTO>> findAll();
}
