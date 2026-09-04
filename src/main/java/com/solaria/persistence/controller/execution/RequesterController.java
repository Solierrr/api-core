package com.solaria.persistence.controller.execution;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.request.execution.RequesterRequestDTO;
import com.solaria.persistence.dto.response.execution.RequesterResponseDTO;
import com.solaria.persistence.openapi.execution.RequesterOpenApi;
import com.solaria.persistence.service.execution.RequesterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/requesters")
@RequiredArgsConstructor
public class RequesterController implements RequesterOpenApi {

    private final RequesterService requesterService;

    @Override
    @PostMapping
    public ResponseEntity<RequesterResponseDTO> save(@Valid @RequestBody RequesterRequestDTO dto) {
        RequesterResponseDTO response = requesterService.save(dto);
        return ResponseEntity.created(URI.create("/api/requesters/" + response.getId())).body(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        requesterService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<RequesterResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(requesterService.findById(id, companyId));
    }

    @Override
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<RequesterResponseDTO>> findByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(requesterService.findByCompany(companyId));
    }
}
