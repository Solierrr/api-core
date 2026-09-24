package com.solaria.persistence.controller.shared;

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

import com.solaria.persistence.dto.request.shared.FluxLogRequestDTO;
import com.solaria.persistence.dto.response.shared.FluxLogResponseDTO;
import com.solaria.persistence.openapi.shared.FluxLogOpenApi;
import com.solaria.persistence.service.shared.FluxLogService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/flux-logs")
@RequiredArgsConstructor
public class FluxLogController implements FluxLogOpenApi {

    private final FluxLogService fluxLogService;

    @Override
    @PostMapping
    public ResponseEntity<FluxLogResponseDTO> record(@Valid @RequestBody FluxLogRequestDTO dto) {
        FluxLogResponseDTO response = fluxLogService.record(dto);
        return ResponseEntity.created(URI.create("/api/flux-logs/" + response.getId())).body(response);
    }

    @Override
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FluxLogResponseDTO>> findByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(fluxLogService.findByUser(userId));
    }
}
