package com.solaria.persistence.controller.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.response.professional.ProfessionResponseDTO;
import com.solaria.persistence.openapi.professional.ProfessionOpenApi;
import com.solaria.persistence.service.professional.ProfessionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/professions")
@RequiredArgsConstructor
public class ProfessionController implements ProfessionOpenApi {

    private final ProfessionService professionService;

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
