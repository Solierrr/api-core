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

import com.solaria.persistence.dto.request.professional.CertificationRecordRequestDTO;
import com.solaria.persistence.dto.response.professional.CertificationRecordResponseDTO;
import com.solaria.persistence.openapi.professional.CertificationRecordOpenApi;
import com.solaria.persistence.service.professional.CertificationRecordService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/certification-records")
@RequiredArgsConstructor
public class CertificationRecordController implements CertificationRecordOpenApi {

    private final CertificationRecordService certificationRecordService;

    @Override
    @PostMapping
    public ResponseEntity<CertificationRecordResponseDTO> save(@Valid @RequestBody CertificationRecordRequestDTO dto) {
        CertificationRecordResponseDTO response = certificationRecordService.save(dto);
        return ResponseEntity.created(URI.create("/api/certification-records/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<CertificationRecordResponseDTO> update(@PathVariable UUID id,
            @Valid @RequestBody CertificationRecordRequestDTO dto) {
        return ResponseEntity.ok(certificationRecordService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        certificationRecordService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<CertificationRecordResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(certificationRecordService.findById(id));
    }

    @Override
    @GetMapping("/professional-registration/{professionalRegistrationId}")
    public ResponseEntity<List<CertificationRecordResponseDTO>> findByProfessionalRegistration(
            @PathVariable UUID professionalRegistrationId) {
        return ResponseEntity.ok(certificationRecordService.findByProfessionalRegistration(professionalRegistrationId));
    }
}
