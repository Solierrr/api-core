package com.solaria.persistence.controller;

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

import com.solaria.persistence.dto.request.ConnectionRequestDTO;
import com.solaria.persistence.dto.response.UserResponseDTO;
import com.solaria.persistence.openapi.ConnectionOpenApi;
import com.solaria.persistence.service.ConnectionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionController implements ConnectionOpenApi {

    private final ConnectionService connectionService;

    @Override
    @PostMapping
    public ResponseEntity<UserResponseDTO> save(@Valid @RequestBody ConnectionRequestDTO dto) {
        UserResponseDTO response = connectionService.save(dto);
        return ResponseEntity.created(URI.create("/api/connections/" + response.getId())).body(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        return ResponseEntity.ok(connectionService.findAll());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(connectionService.findById(id));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        connectionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
