package com.solaria.persistence.openapi.order;

import com.solaria.persistence.dto.request.order.CheckoutRequestDTO;
import com.solaria.persistence.dto.response.order.OrderResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(name = "Pedidos", description = "Compra de unidades de ofertas com pagamento fictício")
public interface OrderOpenApi {

    @Operation(summary = "Finaliza a compra do carrinho",
            description = "Atômico: trava as ofertas, confere o estoque, desconta as unidades, grava o pedido com a taxa do comprador e o pagamento fictício, e esvazia o carrinho.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido pago"),
            @ApiResponse(responseCode = "403", description = "Usuário não pode comprar"),
            @ApiResponse(responseCode = "409", description = "Estoque insuficiente; a resposta lista os itens"),
            @ApiResponse(responseCode = "422", description = "Carrinho vazio")
    })
    ResponseEntity<OrderResponseDTO> checkout(CheckoutRequestDTO request);

    @Operation(summary = "Lista os pedidos do usuário")
    List<OrderResponseDTO> list();

    @Operation(summary = "Detalha um pedido do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    OrderResponseDTO get(UUID id);

    @Operation(summary = "Cancela um pedido pago",
            description = "Devolve as unidades ao estoque e marca o pagamento como estornado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido cancelado"),
            @ApiResponse(responseCode = "422", description = "Pedido não está pago")
    })
    OrderResponseDTO cancel(UUID id);
}
