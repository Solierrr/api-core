package com.solaria.persistence.controller.professional;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.request.professional.CertificationRequestDTO;
import com.solaria.persistence.dto.response.professional.CertificationResponseDTO;
import com.solaria.persistence.openapi.professional.CertificationOpenApi;
import com.solaria.persistence.service.professional.CertificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/certifications")
@RequiredArgsConstructor
public class CertificationController implements CertificationOpenApi {

    private final CertificationService certificationService;

    @Override
    @PostMapping
    public ResponseEntity<CertificationResponseDTO> save(@Valid @RequestBody CertificationRequestDTO dto) {
        CertificationResponseDTO response = certificationService.save(dto);
        return ResponseEntity.created(URI.create("/api/certifications/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<CertificationResponseDTO> update(@PathVariable UUID id,
                                                              @Valid @RequestBody CertificationRequestDTO dto) {
        return ResponseEntity.ok(certificationService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        certificationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

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
