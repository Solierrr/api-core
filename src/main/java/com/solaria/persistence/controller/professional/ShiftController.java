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

import com.solaria.persistence.dto.request.professional.ShiftRequestDTO;
import com.solaria.persistence.dto.response.professional.ShiftResponseDTO;
import com.solaria.persistence.openapi.professional.ShiftOpenApi;
import com.solaria.persistence.service.professional.ShiftService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController implements ShiftOpenApi {

    private final ShiftService shiftService;

    @Override
    @PostMapping
    public ResponseEntity<ShiftResponseDTO> save(@Valid @RequestBody ShiftRequestDTO dto) {
        ShiftResponseDTO response = shiftService.save(dto);
        return ResponseEntity.created(URI.create("/api/shifts/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ShiftResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody ShiftRequestDTO dto) {
        return ResponseEntity.ok(shiftService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        shiftService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ShiftResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(shiftService.findById(id));
    }

    @Override
    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<List<ShiftResponseDTO>> findByTechnician(@PathVariable UUID technicianId) {
        return ResponseEntity.ok(shiftService.findByTechnician(technicianId));
    }
}
