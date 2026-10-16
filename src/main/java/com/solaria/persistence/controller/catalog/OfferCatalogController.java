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

@RestController
@RequestMapping("/api/catalog/offers")
@RequiredArgsConstructor
public class OfferCatalogController {
    private final OfferCatalogService offerCatalogService;

    @GetMapping
    public ResponseEntity<List<CatalogOfferResponseDTO>> findAll(@RequestParam(defaultValue = "pt-BR") String locale,
            @RequestParam(required = false) ProductType productType) {
        return ResponseEntity.ok(offerCatalogService.findAll(locale, productType));
    }

    @GetMapping("/{companySlug}/{offerSlug}")
    public ResponseEntity<CatalogOfferResponseDTO> findBySlug(
            @PathVariable String companySlug,
            @PathVariable String offerSlug,
            @RequestParam(defaultValue = "pt-BR") String locale) {
        return ResponseEntity.ok(offerCatalogService.findBySlug(companySlug, offerSlug, locale));
    }
}
