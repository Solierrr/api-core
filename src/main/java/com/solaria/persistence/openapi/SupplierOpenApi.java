package com.solaria.persistence.openapi;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.SupplierRequestDTO;
import com.solaria.persistence.dto.request.SupplierSearchFilterDTO;
import com.solaria.persistence.dto.response.SupplierResponseDTO;
import com.solaria.persistence.dto.response.SupplierSearchResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Fornecedores", description = "Gerenciamento dos fornecedores e do seu ciclo de vida por status")
public interface SupplierOpenApi {

    @Operation(
        summary = "Cria um novo fornecedor",
        description = "Exige empresa com status APPROVED, sem o papel oposto (Requester) já atribuído e único fornecedor por empresa; nasce sempre com status ACTIVE."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Fornecedor criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<SupplierResponseDTO> save(SupplierRequestDTO dto);

    @Operation(
        summary = "Pesquisa fornecedores",
        description = "Pesquisa fornecedores por texto livre e filtros de localização e tipo de negócio, com resultado paginado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pesquisa realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Filtros de pesquisa inválidos")
    })
    ResponseEntity<SupplierSearchResponseDTO> search(SupplierSearchFilterDTO filters);

    @Operation(summary = "Busca um fornecedor pelo id e pela empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fornecedor encontrado"),
            @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado")
    })
    ResponseEntity<SupplierResponseDTO> findById(UUID id, UUID companyId);

    @Operation(summary = "Lista os fornecedores de uma empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    })
    ResponseEntity<List<SupplierResponseDTO>> findByCompany(UUID companyId);

    @Operation(
        summary = "Ativa um fornecedor",
        description = "Sem exclusão física do fornecedor: o ciclo de vida é controlado por transições de status (ACTIVE⇄SUSPENDED, →DEACTIVATED)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fornecedor ativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado")
    })
    ResponseEntity<SupplierResponseDTO> activate(UUID id);

    @Operation(
        summary = "Suspende um fornecedor",
        description = "Sem exclusão física do fornecedor: o ciclo de vida é controlado por transições de status (ACTIVE⇄SUSPENDED, →DEACTIVATED)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fornecedor suspenso com sucesso"),
            @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado")
    })
    ResponseEntity<SupplierResponseDTO> suspend(UUID id);

    @Operation(
        summary = "Desativa um fornecedor",
        description = "Sem exclusão física do fornecedor: o ciclo de vida é controlado por transições de status (ACTIVE⇄SUSPENDED, →DEACTIVATED)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fornecedor desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado")
    })
    ResponseEntity<SupplierResponseDTO> deactivate(UUID id);
}
