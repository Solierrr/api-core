package com.solaria.persistence.controller.identity;

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

import com.solaria.persistence.dto.request.identity.PositionPermissionRequestDTO;
import com.solaria.persistence.dto.response.identity.PositionPermissionResponseDTO;
import com.solaria.persistence.openapi.identity.PositionPermissionOpenApi;
import com.solaria.persistence.service.identity.PositionPermissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/position-permissions")
@RequiredArgsConstructor
public class PositionPermissionController implements PositionPermissionOpenApi {

    private final PositionPermissionService positionPermissionService;

    @Override
    @PostMapping
    public ResponseEntity<PositionPermissionResponseDTO> grant(@Valid @RequestBody PositionPermissionRequestDTO dto) {
        PositionPermissionResponseDTO response = positionPermissionService.grant(dto);
        return ResponseEntity.created(URI.create("/api/position-permissions/" + response.getId())).body(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable UUID id) {
        positionPermissionService.revoke(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/position/{positionId}")
    public ResponseEntity<List<PositionPermissionResponseDTO>> findByPosition(@PathVariable UUID positionId) {
        return ResponseEntity.ok(positionPermissionService.findByPosition(positionId));
    }
}
