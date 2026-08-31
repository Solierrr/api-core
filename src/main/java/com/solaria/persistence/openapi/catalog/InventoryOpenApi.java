package com.solaria.persistence.openapi.catalog;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.patch.catalog.UpdateQuantityDTO;
import com.solaria.persistence.dto.request.catalog.InventoryRequestDTO;
import com.solaria.persistence.dto.response.catalog.InventoryResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Estoque", description = "Gerenciamento dos itens de estoque")
public interface InventoryOpenApi {

    @Operation(
        summary = "Cria um novo item de estoque",
        description = "A quantidade não pode ser negativa, deve existir apenas um registro por par fornecedor+modelo, e o fornecedor precisa estar ativo e com assinatura em dia."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item de estoque criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para criação do item de estoque")
    })
    ResponseEntity<InventoryResponseDTO> save(InventoryRequestDTO dto);

    @Operation(
        summary = "Atualiza a quantidade de um item de estoque",
        description = "A quantidade não pode ser negativa e o fornecedor precisa estar ativo e com assinatura em dia."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quantidade do item de estoque atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização da quantidade"),
            @ApiResponse(responseCode = "404", description = "Item de estoque não encontrado")
    })
    ResponseEntity<InventoryResponseDTO> updateQuantity(UUID id, UpdateQuantityDTO dto);

    @Operation(summary = "Remove um item de estoque pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item de estoque removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item de estoque não encontrado")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca um item de estoque pelo id e pela empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item de estoque encontrado"),
            @ApiResponse(responseCode = "404", description = "Item de estoque não encontrado")
    })
    ResponseEntity<InventoryResponseDTO> findById(UUID id, UUID companyId);

    @Operation(summary = "Lista os itens de estoque de um fornecedor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de itens de estoque retornada com sucesso")
    })
    ResponseEntity<List<InventoryResponseDTO>> findBySupplier(UUID supplierId);

    @Operation(summary = "Lista todos os itens de estoque de uma empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de itens de estoque retornada com sucesso")
    })
    ResponseEntity<List<InventoryResponseDTO>> findAllByCompany(UUID companyId);
}
