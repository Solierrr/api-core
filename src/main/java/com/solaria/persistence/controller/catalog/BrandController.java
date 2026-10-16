package com.solaria.persistence.controller.catalog;

import com.solaria.persistence.dto.request.catalog.*;
import com.solaria.persistence.dto.response.catalog.*;
import com.solaria.persistence.openapi.catalog.BrandOpenApi;
import com.solaria.persistence.service.catalog.BrandService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController implements BrandOpenApi {
    private final BrandService service;
    private final com.solaria.persistence.service.shared.CloudinaryService cloudinary;
    private final com.solaria.persistence.security.rbac.RbacAuthorizationService rbac;

    @PostMapping(value = "/logo", consumes = "multipart/form-data")
    public java.util.Map<String, String> logo(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        var result =
                cloudinary.upload(
                        file,
                        "brands/" + rbac.getCurrentCompanyId(),
                        null,
                        false,
                        java.util.Set.of("image/jpeg", "image/png", "image/webp"),
                        8 * 1024 * 1024);
        return java.util.Map.of("url", result.url());
    }

    @GetMapping
    public List<BrandResponseDTO> list() {
        return service.list();
    }

    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @PostMapping
    public BrandResponseDTO create(@Valid @RequestBody BrandRequestDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public BrandResponseDTO update(@PathVariable UUID id, @Valid @RequestBody BrandRequestDTO dto) {
        return service.update(id, dto);
    }

    @org.springframework.web.bind.annotation.ResponseStatus(
            org.springframework.http.HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void archive(@PathVariable UUID id) {
        service.archive(id);
    }

    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @PostMapping("/{id}/requests")
    public BrandRequestResponseDTO submit(
            @PathVariable UUID id, @Valid @RequestBody BrandRequestDTO dto) {
        return service.submit(id, dto);
    }
}
