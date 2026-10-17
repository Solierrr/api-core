package com.solaria.persistence.controller.catalog;

import com.solaria.persistence.dto.request.catalog.*;
import com.solaria.persistence.dto.response.catalog.*;
import com.solaria.persistence.openapi.catalog.BrandRequestOpenApi;
import com.solaria.persistence.service.catalog.BrandService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class BrandRequestController implements BrandRequestOpenApi {
    private final BrandService service;

    @GetMapping("/api/brand-requests")
    public List<BrandRequestResponseDTO> own() {
        return service.listRequests(false);
    }

    @GetMapping("/api/brand-review")
    public List<BrandRequestResponseDTO> review() {
        return service.listRequests(true);
    }

    @PostMapping("/api/brand-requests/{id}/cancel")
    public BrandRequestResponseDTO cancel(
            @PathVariable UUID id, @Valid @RequestBody BrandDecisionDTO dto) {
        return service.cancel(id, dto);
    }

    @PostMapping("/api/brand-review/{id}/decision")
    public BrandRequestResponseDTO decide(
            @PathVariable UUID id,
            @RequestParam boolean approved,
            @Valid @RequestBody BrandDecisionDTO dto) {
        return service.decide(id, dto, approved);
    }
}
