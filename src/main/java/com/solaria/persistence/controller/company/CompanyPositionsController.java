package com.solaria.persistence.controller.company;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.request.company.CompanyPositionsRequestDTO;
import com.solaria.persistence.dto.response.company.CompanyPositionsResponseDTO;
import com.solaria.persistence.openapi.company.CompanyPositionsOpenApi;
import com.solaria.persistence.service.company.CompanyPositionsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/company-positions")
@RequiredArgsConstructor
public class CompanyPositionsController implements CompanyPositionsOpenApi {

    private final CompanyPositionsService companyPositionsService;

    @Override
    @PostMapping
    public ResponseEntity<CompanyPositionsResponseDTO> save(@Valid @RequestBody CompanyPositionsRequestDTO dto) {
        CompanyPositionsResponseDTO response = companyPositionsService.save(dto);
        return ResponseEntity.created(URI.create("/api/company-positions/" + response.getId())).body(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        companyPositionsService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<CompanyPositionsResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(companyPositionsService.findById(id, companyId));
    }

    @Override
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<CompanyPositionsResponseDTO>> findAllByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(companyPositionsService.findAllByCompany(companyId));
    }
}
