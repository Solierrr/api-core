package com.solaria.persistence.controller.identity;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.request.identity.UserRequestDTO;
import com.solaria.persistence.dto.request.identity.UpdateOwnUserRequestDTO;
import com.solaria.persistence.dto.response.identity.UserResponseDTO;
import com.solaria.persistence.dto.response.identity.UserSummaryResponseDTO;
import com.solaria.persistence.openapi.identity.UserOpenApi;
import com.solaria.persistence.service.identity.UserService;

import jakarta.validation.Valid;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserOpenApi {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> findMe(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.findMe(UUID.fromString(jwt.getSubject())));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponseDTO> updateMe(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateOwnUserRequestDTO dto) {
        return ResponseEntity.ok(userService.updateMe(UUID.fromString(jwt.getSubject()), dto));
    }

    @GetMapping("/auth/{authId}/summary")
    public ResponseEntity<UserSummaryResponseDTO> findSummaryByAuthId(@PathVariable UUID authId) {
        return ResponseEntity.ok(userService.findSummaryByAuthId(authId));
    }

    @Override
    @PostMapping
    public ResponseEntity<UserResponseDTO> save(@Valid @RequestBody UserRequestDTO dto) {
        UserResponseDTO response = userService.save(dto);
        return ResponseEntity.created(URI.create("/api/users/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody UserRequestDTO dto) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

}
