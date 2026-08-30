package com.solaria.persistence.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.request.SupplierRequestDTO;
import com.solaria.persistence.dto.request.SupplierSearchFilterDTO;
import com.solaria.persistence.dto.response.SupplierResponseDTO;
import com.solaria.persistence.dto.response.SupplierSearchResponseDTO;
import com.solaria.persistence.openapi.SupplierOpenApi;
import com.solaria.persistence.service.SupplierService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController implements SupplierOpenApi {

    private final SupplierService supplierService;

    @Override
    @PostMapping
    public ResponseEntity<SupplierResponseDTO> save(@Valid @RequestBody SupplierRequestDTO dto) {
        SupplierResponseDTO response = supplierService.save(dto);
        return ResponseEntity.created(URI.create("/api/suppliers/" + response.getId())).body(response);
    }

    @Override
    @PostMapping("/search")
    public ResponseEntity<SupplierSearchResponseDTO> search(
            @Valid @RequestBody SupplierSearchFilterDTO filters) {
        return ResponseEntity.ok(supplierService.search(filters));
    }

    @Override
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<SupplierResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(supplierService.findById(id, companyId));
    }

    @Override
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<SupplierResponseDTO>> findByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(supplierService.findByCompany(companyId));
    }

    @Override
    @PostMapping("/{id}/activation")
    public ResponseEntity<SupplierResponseDTO> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(supplierService.activate(id));
    }

    @Override
    @PostMapping("/{id}/suspension")
    public ResponseEntity<SupplierResponseDTO> suspend(@PathVariable UUID id) {
        return ResponseEntity.ok(supplierService.suspend(id));
    }

    @Override
    @PostMapping("/{id}/deactivation")
    public ResponseEntity<SupplierResponseDTO> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(supplierService.deactivate(id));
    }
}
