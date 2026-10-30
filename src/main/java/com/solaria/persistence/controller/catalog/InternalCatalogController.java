package com.solaria.persistence.controller.catalog;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.domain.enums.catalog.InfrastructureType;
import com.solaria.persistence.dto.response.catalog.InfrastructureOfferResponseDTO;
import com.solaria.persistence.dto.response.catalog.InternalProfessionResponseDTO;
import com.solaria.persistence.dto.response.professional.AccreditedTechnicianResponseDTO;
import com.solaria.persistence.openapi.catalog.InternalCatalogOpenApi;
import com.solaria.persistence.service.catalog.InternalCatalogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/catalog")
@RequiredArgsConstructor
public class InternalCatalogController implements InternalCatalogOpenApi {
    private final InternalCatalogService internalCatalogService;

    @Override
    @GetMapping("/professions")
    public ResponseEntity<List<InternalProfessionResponseDTO>> professions() {
        return ResponseEntity.ok(internalCatalogService.professions());
    }

    @Override
    @GetMapping("/infrastructure-offers")
    public ResponseEntity<List<InfrastructureOfferResponseDTO>> infrastructureOffers(
            @RequestParam(required = false) InfrastructureType infrastructureType,
            @RequestParam(required = false) String brand,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(internalCatalogService.infrastructureOffers(infrastructureType, brand, limit));
    }

    @Override
    @GetMapping("/accredited-technicians")
    public ResponseEntity<List<AccreditedTechnicianResponseDTO>> accreditedTechnicians(
            @RequestParam(required = false) String profession,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(internalCatalogService.accreditedTechnicians(profession, city, limit));
    }
}
