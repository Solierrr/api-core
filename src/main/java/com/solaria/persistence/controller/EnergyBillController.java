package com.solaria.persistence.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.solaria.persistence.dto.request.EnergyBillRequestDTO;
import com.solaria.persistence.dto.response.EnergyBillResponseDTO;
import com.solaria.persistence.openapi.EnergyBillOpenApi;
import com.solaria.persistence.service.EnergyBillService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/energy-bills")
@RequiredArgsConstructor
public class EnergyBillController implements EnergyBillOpenApi {

    private final EnergyBillService energyBillService;

    @Override
    @PostMapping
    public ResponseEntity<EnergyBillResponseDTO> save(@Valid @RequestBody EnergyBillRequestDTO dto) {
        EnergyBillResponseDTO response = energyBillService.save(dto);
        return ResponseEntity.created(URI.create("/api/energy-bills/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<EnergyBillResponseDTO> update(@PathVariable UUID id,
                                                          @Valid @RequestBody EnergyBillRequestDTO dto) {
        return ResponseEntity.ok(energyBillService.update(id, dto));
    }

    @Override
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<EnergyBillResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(energyBillService.findById(id, companyId));
    }

    @Override
    @GetMapping("/local-unit/{localUnitId}")
    public ResponseEntity<List<EnergyBillResponseDTO>> findByLocalUnit(@PathVariable UUID localUnitId) {
        return ResponseEntity.ok(energyBillService.findByLocalUnit(localUnitId));
    }

    @Override
    @PutMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EnergyBillResponseDTO> attachPhoto(@PathVariable UUID id,
                                                                @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(energyBillService.attachPhoto(id, file));
    }

    @Override
    @DeleteMapping("/{id}/photo")
    public ResponseEntity<Void> removePhoto(@PathVariable UUID id) {
        energyBillService.removePhoto(id);
        return ResponseEntity.noContent().build();
    }
}
