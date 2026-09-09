package com.solaria.persistence.controller.identity;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.request.identity.InternalUserProvisionRequestDTO;
import com.solaria.persistence.dto.response.identity.InternalUserProvisionResponseDTO;
import com.solaria.persistence.service.identity.InternalUserProvisioningService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// Controller M2M chamado por api-auth (via service token) para garantir que um User com o authId
// do evento USER_REGISTERED exista em dbsolier
@RestController
@RequestMapping("/internal/users")
@Tag(name = "Provisionamento Interno de Usuários", description = "Endpoint M2M usado pelo api-auth")
public class InternalUserProvisioningController {

    private final InternalUserProvisioningService provisioningService;

    public InternalUserProvisioningController(InternalUserProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    // Cria/confirma User com o authId recebido -> sempre responde 200
    // idempotencia da operação garantida por authId
    @PostMapping
    @Operation(summary = "Provisiona ou confirma um usuário a partir do authId")
    public ResponseEntity<InternalUserProvisionResponseDTO> provision(
            @Valid @RequestBody InternalUserProvisionRequestDTO dto) {
        return ResponseEntity.ok(provisioningService.provision(dto.getAuthId()));
    }
}
