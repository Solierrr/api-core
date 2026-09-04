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

import com.solaria.persistence.dto.request.professional.ProfessionRequestDTO;
import com.solaria.persistence.dto.response.professional.ProfessionResponseDTO;
import com.solaria.persistence.openapi.professional.ProfessionOpenApi;
import com.solaria.persistence.service.professional.ProfessionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/professions")
@RequiredArgsConstructor
public class ProfessionController implements ProfessionOpenApi {

    private final ProfessionService professionService;

    @Override
    @PostMapping
    public ResponseEntity<ProfessionResponseDTO> save(@Valid @RequestBody ProfessionRequestDTO dto) {
        ProfessionResponseDTO response = professionService.save(dto);
        return ResponseEntity.created(URI.create("/api/professions/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ProfessionResponseDTO> update(@PathVariable UUID id,
                                                           @Valid @RequestBody ProfessionRequestDTO dto) {
        return ResponseEntity.ok(professionService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        professionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ProfessionResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(professionService.findById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<ProfessionResponseDTO>> findAll() {
        return ResponseEntity.ok(professionService.findAll());
    }
}
