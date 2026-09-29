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

@RestController
@RequestMapping("/api/catalog/technicians")
@RequiredArgsConstructor
public class TechnicianCatalogController {
    private final TechnicianCatalogService technicianCatalogService;

    @GetMapping
    public ResponseEntity<List<CatalogTechnicianResponseDTO>> findAll() {
        return ResponseEntity.ok(technicianCatalogService.findAll());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<CatalogTechnicianResponseDTO> findBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(technicianCatalogService.findBySlug(slug));
    }
}
