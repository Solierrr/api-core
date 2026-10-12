package com.solaria.persistence.controller.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.response.identity.PermissionResponseDTO;
import com.solaria.persistence.openapi.identity.PermissionOpenApi;
import com.solaria.persistence.service.identity.PermissionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController implements PermissionOpenApi {

    private final PermissionService permissionService;

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
