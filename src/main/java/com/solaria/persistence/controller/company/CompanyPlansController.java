package com.solaria.persistence.controller.company;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.response.company.CompanyPlansResponseDTO;
import com.solaria.persistence.openapi.company.CompanyPlansOpenApi;
import com.solaria.persistence.service.company.CompanyPlansService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/company-plans")
@RequiredArgsConstructor
public class CompanyPlansController implements CompanyPlansOpenApi {

    private final CompanyPlansService companyPlansService;

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<CompanyPlansResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(companyPlansService.findById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<CompanyPlansResponseDTO>> findAll() {
        return ResponseEntity.ok(companyPlansService.findAll());
    }
}
