package com.solaria.persistence.openapi.order;

import com.solaria.persistence.dto.response.order.SaleResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Vendas", description = "Vendas da empresa fornecedora")
public interface SalesOpenApi {

    @Operation(summary = "Lista os itens vendidos pela empresa do usuário",
            description = "Inclui pedidos cancelados, com o status do pedido.")
    List<SaleResponseDTO> list();
}
