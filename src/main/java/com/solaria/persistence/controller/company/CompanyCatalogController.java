package com.solaria.persistence.controller.company;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.response.company.CatalogCompanyResponseDTO;
import com.solaria.persistence.service.company.CompanyCatalogService;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

@RestController
@RequestMapping("/api/catalog/companies")
@RequiredArgsConstructor
@Tag(name = "Catálogo Público", description = "Vitrine pública de empresas, ofertas e técnicos; não exige autenticação")
public class CompanyCatalogController {
    private final CompanyCatalogService companyCatalogService;

    @Operation(summary = "Lista as empresas do catálogo público")
    @SecurityRequirements
    @GetMapping
    public ResponseEntity<List<CatalogCompanyResponseDTO>> findAll() {
        return ResponseEntity.ok(companyCatalogService.findAll());
    }

    @Operation(summary = "Consulta uma empresa do catálogo público pelo slug")
    @SecurityRequirements
    @GetMapping("/{slug}")
    public ResponseEntity<CatalogCompanyResponseDTO> findBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(companyCatalogService.findBySlug(slug));
    }
}
