package com.solaria.persistence.controller.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.response.professional.CertificationResponseDTO;
import com.solaria.persistence.openapi.professional.CertificationOpenApi;
import com.solaria.persistence.service.professional.CertificationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/certifications")
@RequiredArgsConstructor
public class CertificationController implements CertificationOpenApi {

    private final CertificationService certificationService;

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<CertificationResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(certificationService.findById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<CertificationResponseDTO>> findAll() {
        return ResponseEntity.ok(certificationService.findAll());
    }
}
