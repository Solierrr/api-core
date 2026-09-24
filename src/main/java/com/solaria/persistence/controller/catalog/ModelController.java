package com.solaria.persistence.controller.catalog;

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

import com.solaria.persistence.domain.enums.catalog.ModelStatus;
import com.solaria.persistence.dto.request.catalog.ModelRequestDTO;
import com.solaria.persistence.dto.response.catalog.ModelResponseDTO;
import com.solaria.persistence.openapi.catalog.ModelOpenApi;
import com.solaria.persistence.service.catalog.ModelService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class ModelController implements ModelOpenApi {

    private final ModelService modelService;

    @Override
    @PostMapping
    public ResponseEntity<ModelResponseDTO> save(@Valid @RequestBody ModelRequestDTO dto) {
        ModelResponseDTO response = modelService.save(dto);
        return ResponseEntity.created(URI.create("/api/models/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ModelResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody ModelRequestDTO dto) {
        return ResponseEntity.ok(modelService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        modelService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ModelResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(modelService.findById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<ModelResponseDTO>> findAll() {
        return ResponseEntity.ok(modelService.findAll());
    }

    @Override
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ModelResponseDTO>> findByStatus(@PathVariable ModelStatus status) {
        return ResponseEntity.ok(modelService.findByStatus(status));
    }

    @Override
    @PostMapping("/{id}/approval")
    public ResponseEntity<ModelResponseDTO> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(modelService.approve(id));
    }

    @Override
    @PostMapping("/{id}/rejection")
    public ResponseEntity<ModelResponseDTO> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(modelService.reject(id));
    }
}
