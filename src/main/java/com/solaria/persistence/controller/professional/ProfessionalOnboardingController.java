package com.solaria.persistence.controller.professional;

import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.solaria.persistence.dto.professional.ProfessionalOnboardingContract.*;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.service.professional.ProfessionalOnboardingService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@Tag(name = "Cadastro Profissional", description = "Envio e revisão dos documentos que liberam o profissional para os clientes")
public class ProfessionalOnboardingController {
    private final ProfessionalOnboardingService onboarding;
    private final CurrentUserService current;
    private final String serviceToken;

    public ProfessionalOnboardingController(ProfessionalOnboardingService onboarding, CurrentUserService current,
            @Value("${app.mobile.service-token:}") String serviceToken) {
        this.onboarding = onboarding;
        this.current = current;
        this.serviceToken = serviceToken;
    }
    @Operation(summary = "Consulta o cadastro profissional do usuário autenticado")
    @GetMapping("/api/professional-onboarding/me")
    public Profile me() { return onboarding.me(); }
    @Operation(summary = "Salva o rascunho do cadastro profissional")
    @PutMapping("/api/professional-onboarding/me/draft")
    public Profile draft(@Valid @RequestBody Draft draft) { return onboarding.saveDraft(draft); }
    @Operation(summary = "Envia o cadastro profissional para validação")
    @PostMapping("/api/professional-onboarding/me/submissions")
    public SubmissionInfo submit(@RequestBody Submit request) { return onboarding.submit(request.revision()); }
    @Operation(summary = "Consulta um envio do cadastro profissional")
    @GetMapping("/api/professional-onboarding/me/submissions/{id}")
    public SubmissionInfo submission(@PathVariable UUID id) { return onboarding.submission(id); }
    @Operation(summary = "Envia um documento do cadastro profissional")
    @PostMapping("/api/professional-onboarding/me/documents")
    public DocumentInfo upload(@RequestParam String kind, @RequestParam MultipartFile file) throws IOException {
        return onboarding.upload(kind, file);
    }
    @Operation(summary = "Consulta os dados de um documento profissional")
    @GetMapping("/api/professional-onboarding/documents/{id}")
    public DocumentInfo documentInfo(@PathVariable UUID id) {
        var doc = onboarding.document(id);
        return new DocumentInfo(doc.getId(), doc.getKind(), doc.getSha256(), doc.getContentType(), doc.getOriginalFileName());
    }
    @Operation(summary = "Lista a fila de cadastros para revisão")
    @GetMapping("/api/professional-review/queue")
    public java.util.List<ReviewSummary> queue() { return onboarding.reviewQueue(); }
    @Operation(summary = "Baixa o conteúdo de um documento profissional")
    @GetMapping("/api/professional-onboarding/documents/{id}/content")
    public ResponseEntity<byte[]> document(@PathVariable UUID id) {
        var doc = onboarding.document(id);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType(doc.getContentType())).body(onboarding.documentContent(id));
    }
    @Operation(summary = "Consulta os requisitos de documentos de uma profissão")
    @GetMapping("/api/professions/{id}/requirements")
    public Policy policy(@PathVariable UUID id) { return onboarding.policy(id); }
    @Operation(summary = "Consulta se o usuário autenticado pode usar o app")
    @GetMapping("/api/users/me/mobile-eligibility")
    public Eligibility eligibility() { return onboarding.eligibility(current.getCurrentUser().getAuth_id()); }
    @Operation(summary = "Consulta se um usuário pode usar o app")
    @SecurityRequirement(name = "serviceKey")
    @GetMapping("/internal/users/{authId}/mobile-eligibility")
    public Eligibility internal(@PathVariable UUID authId, @RequestHeader(value = "Authorization", defaultValue = "") String token) {
        if (serviceToken.length() < 32) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        if (!MessageDigest.isEqual(("Bearer " + serviceToken).getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return onboarding.eligibility(authId);
    }
    @Operation(summary = "Lista os envios de cadastro profissional")
    @GetMapping("/api/professional-review/submissions")
    public java.util.List<SubmissionInfo> reviews(@RequestParam(defaultValue = "0") int page) { return onboarding.reviews(page); }
    @Operation(summary = "Consulta um envio de cadastro profissional para revisão")
    @GetMapping("/api/professional-review/submissions/{id}")
    public SubmissionInfo review(@PathVariable UUID id) { return onboarding.submission(id); }
    @Operation(summary = "Registra a decisão da revisão de um cadastro")
    @PostMapping("/api/professional-review/submissions/{id}/decision")
    public SubmissionInfo decide(@PathVariable UUID id, @Valid @RequestBody Decision decision) { return onboarding.decide(id, decision); }
    @Operation(summary = "Suspende ou reativa um profissional")
    @PostMapping("/api/professional-review/users/{authId}/suspension")
    public Eligibility suspend(@PathVariable UUID authId, @Valid @RequestBody Suspension decision) { return onboarding.suspend(authId, decision); }
}
