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

import com.solaria.persistence.dto.request.identity.PermissionRequestDTO;
import com.solaria.persistence.dto.response.identity.PermissionResponseDTO;
import com.solaria.persistence.openapi.identity.PermissionOpenApi;
import com.solaria.persistence.service.identity.PermissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController implements PermissionOpenApi {

    private final PermissionService permissionService;

    @Override
    @PostMapping
    public ResponseEntity<PermissionResponseDTO> save(@Valid @RequestBody PermissionRequestDTO dto) {
        PermissionResponseDTO response = permissionService.save(dto);
        return ResponseEntity.created(URI.create("/api/permissions/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<PermissionResponseDTO> update(@PathVariable UUID id,
                                                          @Valid @RequestBody PermissionRequestDTO dto) {
        return ResponseEntity.ok(permissionService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        permissionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<PermissionResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(permissionService.findById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<PermissionResponseDTO>> findAll() {
        return ResponseEntity.ok(permissionService.findAll());
    }
}
