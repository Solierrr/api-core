package com.solaria.persistence.controller.identity;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.request.identity.GrantPlatformAdminRequestDTO;
import com.solaria.persistence.dto.response.identity.PlatformAdminResponseDTO;
import com.solaria.persistence.service.identity.PlatformAdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/platform-admins")
@RequiredArgsConstructor
@Tag(name = "Administradores da Plataforma", description = "Pessoas com administração da plataforma inteira")
public class PlatformAdminController {

    private final PlatformAdminService platformAdminService;

    @Operation(summary = "Consulta se o usuário autenticado administra a plataforma")
    @GetMapping("/me")
    public ResponseEntity<PlatformAdminResponseDTO> findMe() {
        PlatformAdminResponseDTO response = platformAdminService.findMe();
        return response == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @Operation(summary = "Concede a administração da plataforma a um usuário")
    @PostMapping
    public ResponseEntity<PlatformAdminResponseDTO> grant(@Valid @RequestBody GrantPlatformAdminRequestDTO dto) {
        PlatformAdminResponseDTO response = platformAdminService.grant(dto.userId());
        return ResponseEntity.created(URI.create("/api/platform-admins/" + response.id())).body(response);
    }
}
