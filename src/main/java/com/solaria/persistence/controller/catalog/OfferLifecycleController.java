package com.solaria.persistence.controller.catalog;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.solaria.persistence.domain.entity.catalog.OfferStatusHistory;
import com.solaria.persistence.domain.enums.catalog.OfferStatus;
import com.solaria.persistence.service.catalog.OfferLifecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@Tag(name = "Ciclo de Vida de Ofertas", description = "Situação das ofertas e o histórico das mudanças")
public class OfferLifecycleController {
    public record StatusRequest(@NotNull OfferStatus status) {}
    private final OfferLifecycleService lifecycle;
    @Operation(summary = "Altera a situação de uma oferta")
    @PatchMapping("/{id}/status")
    public Map<String,OfferStatus> change(@PathVariable UUID id, @Valid @RequestBody StatusRequest request) {
        return Map.of("commercialStatus", lifecycle.change(id, request.status()));
    }
    @Operation(summary = "Lista o histórico de situações de uma oferta")
    @GetMapping("/{id}/history")
    public List<OfferStatusHistory> history(@PathVariable UUID id) { return lifecycle.history(id); }
}
