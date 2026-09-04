package com.solaria.persistence.openapi.catalog;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.catalog.OfferRequestDTO;
import com.solaria.persistence.dto.response.catalog.OfferResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Offers", description = "Gerenciamento das ofertas comerciais disponibilizadas pelos fornecedores.")
public interface OfferOpenApi {

    @Operation(
        summary = "Cadastra uma nova oferta",
        description = "Fornecedor e modelo são obrigatórios e imutáveis; o preço deve ser maior que zero; exige fornecedor ativo/em dia e modelo aprovado; a validade, quando informada, precisa ser futura."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Oferta criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<OfferResponseDTO> save(OfferRequestDTO dto);

    @Operation(
        summary = "Atualiza os dados de uma oferta existente",
        description = "Fornecedor e modelo são imutáveis; o preço deve ser maior que zero e a validade, quando informada, precisa ser futura."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Oferta atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Oferta não encontrada")
    })
    ResponseEntity<OfferResponseDTO> update(UUID id, OfferRequestDTO dto);

    @Operation(
        summary = "Remove uma oferta pelo identificador",
        description = "Não é permitida a exclusão se a oferta estiver referenciada em algum item de proposta."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Oferta removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Oferta não encontrada")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca uma oferta pelo identificador, escopada à empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Oferta encontrada"),
            @ApiResponse(responseCode = "404", description = "Oferta não encontrada")
    })
    ResponseEntity<OfferResponseDTO> findById(UUID id, UUID companyId);

    @Operation(summary = "Lista as ofertas cadastradas por um fornecedor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso")
    })
    ResponseEntity<List<OfferResponseDTO>> findBySupplier(UUID supplierId);

    @Operation(summary = "Lista todas as ofertas de uma empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso")
    })
    ResponseEntity<List<OfferResponseDTO>> findAllByCompany(UUID companyId);

    @Operation(summary = "Lista o catálogo público de ofertas vigentes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso")
    })
    ResponseEntity<List<OfferResponseDTO>> findCatalog();
}
