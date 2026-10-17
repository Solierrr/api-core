package com.solaria.persistence.openapi.order;

import com.solaria.persistence.dto.request.order.CartItemRequestDTO;
import com.solaria.persistence.dto.response.order.CartResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "Carrinho", description = "Carrinho de compras do usuário autenticado, guardado no servidor")
public interface CartOpenApi {

    @Operation(summary = "Retorna o carrinho do usuário",
            description = "Cria o carrinho vazio no primeiro acesso. Preços, taxa e total são calculados na hora.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrinho do usuário"),
            @ApiResponse(responseCode = "403", description = "Usuário não pode comprar")
    })
    CartResponseDTO get();

    @Operation(summary = "Define a quantidade de uma oferta no carrinho",
            description = "Adiciona a oferta ou troca a quantidade. A quantidade não pode passar do estoque.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrinho atualizado"),
            @ApiResponse(responseCode = "403", description = "Usuário não pode comprar ou oferta é da própria empresa"),
            @ApiResponse(responseCode = "404", description = "Oferta não encontrada"),
            @ApiResponse(responseCode = "409", description = "Estoque insuficiente"),
            @ApiResponse(responseCode = "422", description = "Oferta indisponível para compra")
    })
    CartResponseDTO putItem(UUID offerId, CartItemRequestDTO request);

    @Operation(summary = "Remove uma oferta do carrinho")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrinho atualizado")
    })
    CartResponseDTO removeItem(UUID offerId);
}
