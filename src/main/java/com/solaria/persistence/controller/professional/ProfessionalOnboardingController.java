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

@RestController
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
    @GetMapping("/api/professional-onboarding/me")
    public Profile me() { return onboarding.me(); }
    @PutMapping("/api/professional-onboarding/me/draft")
    public Profile draft(@Valid @RequestBody Draft draft) { return onboarding.saveDraft(draft); }
    @PostMapping("/api/professional-onboarding/me/submissions")
    public SubmissionInfo submit(@RequestBody Submit request) { return onboarding.submit(request.revision()); }
    @GetMapping("/api/professional-onboarding/me/submissions/{id}")
    public SubmissionInfo submission(@PathVariable UUID id) { return onboarding.submission(id); }
    @PostMapping("/api/professional-onboarding/me/documents")
    public DocumentInfo upload(@RequestParam String kind, @RequestParam MultipartFile file) throws IOException {
        return onboarding.upload(kind, file);
    }
    @GetMapping("/api/professional-onboarding/documents/{id}")
    public DocumentInfo documentInfo(@PathVariable UUID id) {
        var doc = onboarding.document(id);
        return new DocumentInfo(doc.getId(), doc.getKind(), doc.getSha256(), doc.getContentType(), doc.getOriginalFileName());
    }
    @GetMapping("/api/professional-review/queue")
    public java.util.List<ReviewSummary> queue() { return onboarding.reviewQueue(); }
    @GetMapping("/api/professional-onboarding/documents/{id}/content")
    public ResponseEntity<byte[]> document(@PathVariable UUID id) {
        var doc = onboarding.document(id);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType(doc.getContentType())).body(onboarding.documentContent(id));
    }
    @GetMapping("/api/professions/{id}/requirements")
    public Policy policy(@PathVariable UUID id) { return onboarding.policy(id); }
    @GetMapping("/api/users/me/mobile-eligibility")
    public Eligibility eligibility() { return onboarding.eligibility(current.getCurrentUser().getAuth_id()); }
    @GetMapping("/internal/users/{authId}/mobile-eligibility")
    public Eligibility internal(@PathVariable UUID authId, @RequestHeader(value = "Authorization", defaultValue = "") String token) {
        if (serviceToken.length() < 32) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        if (!MessageDigest.isEqual(("Bearer " + serviceToken).getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return onboarding.eligibility(authId);
    }
    @GetMapping("/api/professional-review/submissions")
    public java.util.List<SubmissionInfo> reviews(@RequestParam(defaultValue = "0") int page) { return onboarding.reviews(page); }
    @GetMapping("/api/professional-review/submissions/{id}")
    public SubmissionInfo review(@PathVariable UUID id) { return onboarding.submission(id); }
    @PostMapping("/api/professional-review/submissions/{id}/decision")
    public SubmissionInfo decide(@PathVariable UUID id, @Valid @RequestBody Decision decision) { return onboarding.decide(id, decision); }
    @PostMapping("/api/professional-review/users/{authId}/suspension")
    public Eligibility suspend(@PathVariable UUID authId, @Valid @RequestBody Suspension decision) { return onboarding.suspend(authId, decision); }
}
