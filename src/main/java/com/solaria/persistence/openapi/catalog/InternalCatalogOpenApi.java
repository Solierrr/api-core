package com.solaria.persistence.openapi.catalog;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.domain.enums.catalog.InfrastructureType;
import com.solaria.persistence.dto.response.catalog.InfrastructureOfferResponseDTO;
import com.solaria.persistence.dto.response.catalog.InternalProfessionResponseDTO;
import com.solaria.persistence.dto.response.professional.AccreditedTechnicianResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Catálogo interno", description = "Leituras do catálogo para o mcp-database, autenticadas pelo header X-Service-Key com a chave SERVICE_KEY_MCP_DATABASE")
public interface InternalCatalogOpenApi {

    @Operation(summary = "Lista as profissões ordenadas por nome")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de profissões retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Chave de serviço ausente ou inválida")
    })
    ResponseEntity<List<InternalProfessionResponseDTO>> professions();

    @Operation(summary = "Lista ofertas de itens de infraestrutura visíveis no catálogo, da mais barata para a mais cara",
            description = "Aplica a mesma regra de visibilidade do catálogo público; brand filtra por trecho do nome, sem diferenciar maiúsculas; limit entre 1 e 20.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de ofertas retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido"),
            @ApiResponse(responseCode = "401", description = "Chave de serviço ausente ou inválida")
    })
    ResponseEntity<List<InfrastructureOfferResponseDTO>> infrastructureOffers(InfrastructureType infrastructureType,
            String brand, int limit);

    @Operation(summary = "Lista técnicos aprovados com afiliação ativa a empresas, uma linha por afiliação e registro profissional",
            description = "profession filtra por trecho do nome da profissão e city por trecho da cidade da empresa afiliada, sem diferenciar maiúsculas; limit entre 1 e 100.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de técnicos retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido"),
            @ApiResponse(responseCode = "401", description = "Chave de serviço ausente ou inválida")
    })
    ResponseEntity<List<AccreditedTechnicianResponseDTO>> accreditedTechnicians(String profession, String city,
            int limit);
}
