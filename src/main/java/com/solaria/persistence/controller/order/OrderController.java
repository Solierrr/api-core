package com.solaria.persistence.controller.order;

import com.solaria.persistence.dto.request.order.CheckoutRequestDTO;
import com.solaria.persistence.dto.response.order.OrderResponseDTO;
import com.solaria.persistence.openapi.order.OrderOpenApi;
import com.solaria.persistence.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController implements OrderOpenApi {

    private final OrderService orderService;

    @Override
    @PostMapping
    public ResponseEntity<OrderResponseDTO> checkout(@Valid @RequestBody CheckoutRequestDTO request) {
        OrderResponseDTO response = orderService.checkout(request.paymentMethod());
        return ResponseEntity.created(URI.create("/api/orders/" + response.id())).body(response);
    }

    @Override
    @GetMapping
    public List<OrderResponseDTO> list() {
        return orderService.list();
    }

    @Override
    @GetMapping("/{id}")
    public OrderResponseDTO get(@PathVariable UUID id) {
        return orderService.get(id);
    }

    @Override
    @PatchMapping("/{id}/cancel")
    public OrderResponseDTO cancel(@PathVariable UUID id) {
        return orderService.cancel(id);
    }
}
