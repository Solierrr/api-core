package com.solaria.persistence.controller.identity;

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

import com.solaria.persistence.dto.request.identity.PositionRequestDTO;
import com.solaria.persistence.dto.response.identity.PositionResponseDTO;
import com.solaria.persistence.openapi.identity.PositionOpenApi;
import com.solaria.persistence.service.identity.PositionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController implements PositionOpenApi {

    private final PositionService positionService;

    @Override
    @PostMapping
    public ResponseEntity<PositionResponseDTO> save(@Valid @RequestBody PositionRequestDTO dto) {
        PositionResponseDTO response = positionService.save(dto);
        return ResponseEntity.created(URI.create("/api/positions/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<PositionResponseDTO> update(@PathVariable UUID id,
                                                        @Valid @RequestBody PositionRequestDTO dto) {
        return ResponseEntity.ok(positionService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        positionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<PositionResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(positionService.findById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<PositionResponseDTO>> findAll() {
        return ResponseEntity.ok(positionService.findAll());
    }
}
