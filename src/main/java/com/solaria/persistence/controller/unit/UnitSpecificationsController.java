package com.solaria.persistence.controller.unit;

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

import com.solaria.persistence.dto.request.unit.UnitSpecificationsRequestDTO;
import com.solaria.persistence.dto.response.unit.UnitSpecificationsResponseDTO;
import com.solaria.persistence.openapi.unit.UnitSpecificationsOpenApi;
import com.solaria.persistence.service.unit.UnitSpecificationsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/unit-specifications")
@RequiredArgsConstructor
public class UnitSpecificationsController implements UnitSpecificationsOpenApi {

    private final UnitSpecificationsService unitSpecificationsService;

    @Override
    @PostMapping
    public ResponseEntity<UnitSpecificationsResponseDTO> save(@Valid @RequestBody UnitSpecificationsRequestDTO dto) {
        UnitSpecificationsResponseDTO response = unitSpecificationsService.save(dto);
        return ResponseEntity.created(URI.create("/api/unit-specifications/" + response.getId())).body(response);
    }

    @Override
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<UnitSpecificationsResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(unitSpecificationsService.findById(id, companyId));
    }

    @Override
    @GetMapping("/local-unit/{localUnitId}")
    public ResponseEntity<List<UnitSpecificationsResponseDTO>> findByLocalUnit(@PathVariable UUID localUnitId) {
        return ResponseEntity.ok(unitSpecificationsService.findByLocalUnit(localUnitId));
    }
}
