package com.solaria.persistence.openapi.company;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.company.CompanyPlansRequestDTO;
import com.solaria.persistence.dto.response.company.CompanyPlansResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Company Plans", description = "Gerenciamento dos planos de assinatura vinculados às empresas.")
public interface CompanyPlansOpenApi {

    @Operation(
        summary = "Cadastra um novo plano de empresa",
        description = "O valor do plano precisa ser maior que zero."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Plano criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<CompanyPlansResponseDTO> save(CompanyPlansRequestDTO dto);

    @Operation(
        summary = "Atualiza os dados de um plano de empresa existente",
        description = "O valor do plano precisa ser maior que zero."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plano atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Plano não encontrado")
    })
    ResponseEntity<CompanyPlansResponseDTO> update(UUID id, CompanyPlansRequestDTO dto);

    @Operation(
        summary = "Remove um plano de empresa pelo identificador",
        description = "Não é possível excluir um plano que tenha Subscription vinculada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Plano removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Plano não encontrado")
    })
    ResponseEntity<Void> deleteById(UUID id);

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
