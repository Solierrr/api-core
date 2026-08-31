package com.solaria.persistence.controller.company;

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

import com.solaria.persistence.dto.request.company.CompanyPlansRequestDTO;
import com.solaria.persistence.dto.response.company.CompanyPlansResponseDTO;
import com.solaria.persistence.openapi.company.CompanyPlansOpenApi;
import com.solaria.persistence.service.company.CompanyPlansService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/company-plans")
@RequiredArgsConstructor
public class CompanyPlansController implements CompanyPlansOpenApi {

    private final CompanyPlansService companyPlansService;

    @Override
    @PostMapping
    public ResponseEntity<CompanyPlansResponseDTO> save(@Valid @RequestBody CompanyPlansRequestDTO dto) {
        CompanyPlansResponseDTO response = companyPlansService.save(dto);
        return ResponseEntity.created(URI.create("/api/company-plans/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<CompanyPlansResponseDTO> update(@PathVariable UUID id,
                                                            @Valid @RequestBody CompanyPlansRequestDTO dto) {
        return ResponseEntity.ok(companyPlansService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        companyPlansService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

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
