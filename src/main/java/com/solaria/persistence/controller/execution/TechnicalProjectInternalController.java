package com.solaria.persistence.controller.execution;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.service.execution.ProjectRequesterAccessService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Controller M2M chamado pelo api-messenger (via {@code X-Service-Key}) 
 * para saber se o usuário é o demandante do projeto antes de deixá-lo criar a comunidade
 */
@RestController
@RequestMapping("/internal/technical-projects")
@RequiredArgsConstructor
@Tag(name = "Acesso Interno a Projetos Técnicos", description = "Endpoint M2M usado pelo api-messenger")
public class TechnicalProjectInternalController {

    private final ProjectRequesterAccessService requesterAccessService;

    /**
     * @return 204 se o usuário for o demandante do projeto
     * @throws com.solaria.persistence.exception.UnauthorizedAccessException (403) se não for o demandante
     * @throws com.solaria.persistence.exception.ResourceNotFoundException (404) se projeto ou usuário não existirem
     */
    @GetMapping("/{projectId}/requester-access/{authId}")
    @Operation(summary = "Confirma se o usuário é o demandante do projeto")
    public ResponseEntity<Void> requesterAccess(@PathVariable UUID projectId, @PathVariable UUID authId) {
        requesterAccessService.requireRequester(projectId, authId);
        return ResponseEntity.noContent().build();
    }
}
