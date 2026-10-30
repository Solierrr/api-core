package com.solaria.persistence.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import com.solaria.persistence.dto.response.identity.UserResponseDTO;
import com.solaria.persistence.openapi.ConnectionOpenApi;
import com.solaria.persistence.service.ConnectionService;
import com.solaria.persistence.service.ConnectionDirectoryService;
import com.solaria.persistence.dto.response.ConnectionPeopleResponse;
import com.solaria.persistence.dto.response.ConnectionPersonResponse;
import com.solaria.persistence.exception.InvalidFieldException;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionController implements ConnectionOpenApi {

    private final ConnectionService connectionService;
    private final ConnectionDirectoryService directory;

    @Operation(summary = "Busca pessoas para conectar ou já conectadas", operationId = "searchConnectionPeople")
    @GetMapping("/people")
    public ResponseEntity<ConnectionPeopleResponse> people(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "false") boolean connectedOnly) {
        if (page < 0 || page > 10000 || query.length() > 120)
            throw new InvalidFieldException("Busca inválida");
        return ResponseEntity.ok(directory.search(query, page, connectedOnly));
    }

    @Operation(summary = "Consulta o perfil de uma pessoa do diretório de conexões", operationId = "getConnectionPerson")
    @GetMapping("/people/{id}")
    public ResponseEntity<ConnectionPersonResponse> person(@PathVariable UUID id) {
        return ResponseEntity.ok(directory.profile(id));
    }

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
