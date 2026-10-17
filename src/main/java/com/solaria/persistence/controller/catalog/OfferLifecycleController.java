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

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferLifecycleController {
    public record StatusRequest(@NotNull OfferStatus status) {}
    private final OfferLifecycleService lifecycle;
    @PatchMapping("/{id}/status")
    public Map<String,OfferStatus> change(@PathVariable UUID id, @Valid @RequestBody StatusRequest request) {
        return Map.of("commercialStatus", lifecycle.change(id, request.status()));
    }
    @GetMapping("/{id}/history")
    public List<OfferStatusHistory> history(@PathVariable UUID id) { return lifecycle.history(id); }
}
