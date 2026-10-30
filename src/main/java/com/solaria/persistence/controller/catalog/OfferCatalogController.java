package com.solaria.persistence.controller.catalog;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.domain.enums.catalog.ProductType;
import com.solaria.persistence.dto.response.catalog.CatalogOfferResponseDTO;
import com.solaria.persistence.service.catalog.OfferCatalogService;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

@RestController
@RequestMapping("/api/catalog/offers")
@RequiredArgsConstructor
@Tag(name = "Catálogo Público", description = "Vitrine pública de empresas, ofertas e técnicos; não exige autenticação")
public class OfferCatalogController {
    private final OfferCatalogService offerCatalogService;

    @Operation(summary = "Lista as ofertas do catálogo público")
    @SecurityRequirements
    @GetMapping
    public ResponseEntity<List<CatalogOfferResponseDTO>> findAll(@RequestParam(defaultValue = "pt-BR") String locale,
            @RequestParam(required = false) ProductType productType) {
        return ResponseEntity.ok(offerCatalogService.findAll(locale, productType));
    }

    @Operation(summary = "Consulta uma oferta do catálogo público pelos slugs")
    @SecurityRequirements
    @GetMapping("/{companySlug}/{offerSlug}")
    public ResponseEntity<CatalogOfferResponseDTO> findBySlug(
            @PathVariable String companySlug,
            @PathVariable String offerSlug,
            @RequestParam(defaultValue = "pt-BR") String locale) {
        return ResponseEntity.ok(offerCatalogService.findBySlug(companySlug, offerSlug, locale));
    }
}
