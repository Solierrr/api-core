package com.solaria.persistence.openapi.execution;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.response.execution.PersonalTechnicalServiceResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Serviços Técnicos", description = "Gerenciamento dos serviços técnicos e do seu ciclo de vida")
public interface PersonalTechnicalServiceListOpenApi {

    @Operation(
        summary = "Lista os serviços do usuário autenticado",
        description = "Retorna todos os serviços atribuídos ao técnico resolvido pelo subject do token, "
                + "ordenados por criação decrescente e id crescente, sem paginação ou filtros.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "Usuário ainda não provisionado no Core")
    })
    ResponseEntity<List<PersonalTechnicalServiceResponseDTO>> findMine();
}
