package com.solaria.persistence.controller.professional;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.response.professional.CatalogTechnicianResponseDTO;
import com.solaria.persistence.service.professional.TechnicianCatalogService;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

@RestController
@RequestMapping("/api/catalog/technicians")
@RequiredArgsConstructor
@Tag(name = "Catálogo Público", description = "Vitrine pública de empresas, ofertas e técnicos; não exige autenticação")
public class TechnicianCatalogController {
    private final TechnicianCatalogService technicianCatalogService;

    @Operation(summary = "Lista os técnicos do catálogo público")
    @SecurityRequirements
    @GetMapping
    public ResponseEntity<List<CatalogTechnicianResponseDTO>> findAll() {
        return ResponseEntity.ok(technicianCatalogService.findAll());
    }

    @Operation(summary = "Consulta um técnico do catálogo público pelo slug")
    @SecurityRequirements
    @GetMapping("/{slug}")
    public ResponseEntity<CatalogTechnicianResponseDTO> findBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(technicianCatalogService.findBySlug(slug));
    }
}
