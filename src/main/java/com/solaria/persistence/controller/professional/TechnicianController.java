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

import com.solaria.persistence.dto.request.professional.TechnicianRequestDTO;
import com.solaria.persistence.dto.response.professional.TechnicianResponseDTO;
import com.solaria.persistence.openapi.professional.TechnicianOpenApi;
import com.solaria.persistence.service.professional.TechnicianService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
public class TechnicianController implements TechnicianOpenApi {

    private final TechnicianService technicianService;

    @Override
    @PostMapping
    public ResponseEntity<TechnicianResponseDTO> save(@Valid @RequestBody TechnicianRequestDTO dto) {
        TechnicianResponseDTO response = technicianService.save(dto);
        return ResponseEntity.created(URI.create("/api/technicians/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<TechnicianResponseDTO> update(@PathVariable UUID id,
                                                          @Valid @RequestBody TechnicianRequestDTO dto) {
        return ResponseEntity.ok(technicianService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        technicianService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<TechnicianResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(technicianService.findById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<TechnicianResponseDTO>> findAll() {
        return ResponseEntity.ok(technicianService.findAll());
    }
}
