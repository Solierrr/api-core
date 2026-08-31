package com.solaria.persistence.controller.execution;

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

import com.solaria.persistence.dto.request.execution.TechnicalProjectRequestDTO;
import com.solaria.persistence.dto.response.execution.TechnicalProjectResponseDTO;
import com.solaria.persistence.openapi.execution.TechnicalProjectOpenApi;
import com.solaria.persistence.service.execution.TechnicalProjectService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/technical-projects")
@RequiredArgsConstructor
public class TechnicalProjectController implements TechnicalProjectOpenApi {

    private final TechnicalProjectService technicalProjectService;

    @Override
    @PostMapping
    public ResponseEntity<TechnicalProjectResponseDTO> save(@Valid @RequestBody TechnicalProjectRequestDTO dto) {
        TechnicalProjectResponseDTO response = technicalProjectService.save(dto);
        return ResponseEntity.created(URI.create("/api/technical-projects/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<TechnicalProjectResponseDTO> update(@PathVariable UUID id,
                                                                 @Valid @RequestBody TechnicalProjectRequestDTO dto) {
        return ResponseEntity.ok(technicalProjectService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        technicalProjectService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<TechnicalProjectResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(technicalProjectService.findById(id, companyId));
    }

    @Override
    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<TechnicalProjectResponseDTO>> findByRequester(@PathVariable UUID requesterId) {
        return ResponseEntity.ok(technicalProjectService.findByRequester(requesterId));
    }

    @Override
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<TechnicalProjectResponseDTO>> findAllByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(technicalProjectService.findAllByCompany(companyId));
    }
}
