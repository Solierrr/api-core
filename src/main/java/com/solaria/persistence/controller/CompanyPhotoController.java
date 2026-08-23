package com.solaria.persistence.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.solaria.persistence.domain.enums.PhotoType;
import com.solaria.persistence.dto.response.CompanyPhotoResponseDTO;
import com.solaria.persistence.openapi.CompanyPhotoOpenApi;
import com.solaria.persistence.service.CompanyPhotoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/company-photos")
@RequiredArgsConstructor
public class CompanyPhotoController implements CompanyPhotoOpenApi {

    private final CompanyPhotoService companyPhotoService;

    @Override
    @PutMapping(value = "/company/{companyId}/type/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CompanyPhotoResponseDTO> upload(@PathVariable UUID companyId,
                                                             @PathVariable PhotoType type,
                                                             @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(companyPhotoService.upload(companyId, type, file));
    }

    @Override
    @DeleteMapping("/company/{companyId}/type/{type}")
    public ResponseEntity<Void> delete(@PathVariable UUID companyId, @PathVariable PhotoType type) {
        companyPhotoService.delete(companyId, type);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<CompanyPhotoResponseDTO>> findByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(companyPhotoService.findByCompany(companyId));
    }
}
