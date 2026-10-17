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

@RestController
@RequestMapping("/api/catalog/companies")
@RequiredArgsConstructor
public class CompanyCatalogController {
    private final CompanyCatalogService companyCatalogService;

    @GetMapping
    public ResponseEntity<List<CatalogCompanyResponseDTO>> findAll() {
        return ResponseEntity.ok(companyCatalogService.findAll());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<CatalogCompanyResponseDTO> findBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(companyCatalogService.findBySlug(slug));
    }
}
