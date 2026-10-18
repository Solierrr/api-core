package com.solaria.persistence.controller.order;

import com.solaria.persistence.dto.request.order.CartItemRequestDTO;
import com.solaria.persistence.dto.response.order.CartResponseDTO;
import com.solaria.persistence.openapi.order.CartOpenApi;
import com.solaria.persistence.service.order.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController implements CartOpenApi {

    private final CartService cartService;

    @Override
    @GetMapping
    public CartResponseDTO get() {
        return cartService.get();
    }

    @Override
    @PutMapping("/items/{offerId}")
    public CartResponseDTO putItem(@PathVariable UUID offerId, @Valid @RequestBody CartItemRequestDTO request) {
        return cartService.putItem(offerId, request.quantity());
    }

    @Override
    @DeleteMapping("/items/{offerId}")
    public CartResponseDTO removeItem(@PathVariable UUID offerId) {
        return cartService.removeItem(offerId);
    }
}
