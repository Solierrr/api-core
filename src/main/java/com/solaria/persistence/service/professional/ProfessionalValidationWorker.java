package com.solaria.persistence.service.professional;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import com.solaria.persistence.domain.entity.professional.ProfessionalSubmission;
import com.solaria.persistence.dto.professional.ProfessionalOnboardingContract.*;
import com.solaria.persistence.repository.professional.*;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(name = "app.professional-validation.enabled", havingValue = "true")
public class ProfessionalValidationWorker {
    private final ProfessionalSubmissionRepository submissions;
    private final ProfessionalOnboardingRepository profiles;
    private final ProfessionalDocumentRepository documents;
    private final ProfessionalOnboardingService onboarding;
    private final ProfessionalDocumentStorageService storage;
    private final ObjectMapper json;
    private final TransactionTemplate transaction;
    private final RestClient client;
    private final String token;

    public ProfessionalValidationWorker(ProfessionalSubmissionRepository submissions,
            ProfessionalOnboardingRepository profiles, ProfessionalDocumentRepository documents,
            ProfessionalOnboardingService onboarding, ProfessionalDocumentStorageService storage, ObjectMapper json, PlatformTransactionManager manager,
            @Value("${app.professional-validation.base-url:http://localhost:8000}") String baseUrl,
            @Value("${app.professional-validation.service-token:}") String token) {
        this.submissions = submissions;
        this.profiles = profiles;
        this.documents = documents;
        this.onboarding = onboarding;
        this.storage = storage;
        this.json = json;
        this.transaction = new TransactionTemplate(manager);
        this.token = token;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(180));
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    @Scheduled(fixedDelayString = "${app.professional-validation.poll-ms:5000}")
    public void poll() {
        if (token.length() < 32) return;
        List<UUID> ids = submissions.findAvailable(Instant.now(), PageRequest.of(0, 5));
        for (UUID id : ids) process(id);
    }

    public void process(UUID id) {
        ProfessionalSubmission work = transaction.execute(status -> {
            var submission = submissions.lockById(id).orElse(null);
            if (submission == null || submission.getAvailableAt().isAfter(Instant.now())
                    || !List.of("QUEUED", "RETRY_PENDING", "VALIDATING").contains(submission.getStatus())) return null;
            submission.setStatus("VALIDATING");
            submission.setAttempts(submission.getAttempts() + 1);
            submission.setAvailableAt(Instant.now().plusSeconds(300));
            return submissions.save(submission);
        });
        if (work == null) return;
        String validationResult = null;
        try {
            Draft draft = onboarding.readDraft(work.getSnapshotJson());
            Policy policy = onboarding.readPolicy(work.getPolicyJson());
            var profile = profiles.findById(work.getOnboardingId()).orElseThrow();
            var evidence = draft.documentIds().stream().map(idValue -> {
                var doc = documents.findById(idValue).orElseThrow();
                if (!doc.getUserId().equals(profile.getUserId())) throw new IllegalStateException("Document owner mismatch");
                return Map.of("id", doc.getId(), "kind", doc.getKind(), "sha256", doc.getSha256(),
                        "contentBase64", Base64.getEncoder().encodeToString(storage.read(doc)), "contentType", doc.getContentType());
            }).toList();
            var request = new java.util.HashMap<String, Object>(Map.of("submissionId", work.getId(), "revision", work.getRevision(),
                    "policyVersion", policy.version(), "professionId", draft.professionId(),
                    "expectedIdentity", Map.of("name", draft.name(), "cpf", draft.cpf(), "birthDate", draft.birthDate()),
                    "expectedProfession", policy.professionName(), "criteria", policy.criteria(), "documents", evidence));
            if (policy.requiresRegistration()) {
                var registration = new java.util.HashMap<String, Object>();
                registration.put("council", draft.council());
                registration.put("number", draft.registrationNumber());
                registration.put("expiration", draft.registrationExpiration());
                request.put("expectedRegistration", registration);
            }
            String result = work.getResultJson();
            if (result == null || !passesPolicy(json, result, work.getId(), work.getRevision(), policy, draft.documentIds())) {
                result = client.post().uri("/api/v1/professionals/validate")
                        .header("Authorization", "Bearer " + token).body(request).retrieve().body(String.class);
            }
            if (passesPolicy(json, result, work.getId(), work.getRevision(), policy, draft.documentIds())) validationResult = result;
            String completedResult = result;
            transaction.executeWithoutResult(status -> finish(work, completedResult));
        } catch (Exception exception) {
            String confirmedResult = validationResult;
            transaction.executeWithoutResult(status -> {
                var current = submissions.lockById(work.getId()).orElseThrow();
                if (!"VALIDATING".equals(current.getStatus()) || current.getAttempts() != work.getAttempts()) return;
                current.setStatus("RETRY_PENDING");
                current.setAvailableAt(Instant.now().plusSeconds(Math.min(3600, 30L * current.getAttempts())));
                current.setResultJson(confirmedResult != null ? confirmedResult
                        : "{\"status\":\"TECHNICAL_ERROR\",\"reason\":\"Validation temporarily unavailable\"}");
                submissions.save(current);
            });
        }
    }

    private void finish(ProfessionalSubmission work, String result) {
        var current = submissions.lockById(work.getId()).orElseThrow();
        if (!"VALIDATING".equals(current.getStatus()) || current.getAttempts() != work.getAttempts()) return;
        var profile = profiles.lockById(current.getOnboardingId()).orElseThrow();
        Policy policy = onboarding.readPolicy(current.getPolicyJson());
        Draft draft = onboarding.readDraft(current.getSnapshotJson());
        var kinds = new java.util.HashMap<UUID, String>();
        for (UUID documentId : draft.documentIds()) kinds.put(documentId, documents.findById(documentId).orElseThrow().getKind());
        boolean passed = passesPolicy(json, result, current.getId(), current.getRevision(), policy, draft.documentIds())
                && passesEvidenceKinds(json, result, kinds);
        current.setResultJson(result);
        if (passed && profile.getApprovedVersion() == current.getBaseApprovedVersion()) {
            var confirmedDocuments = new HashSet<UUID>();
            for (var criterion : json.readTree(result).path("criteriaResults")) {
                for (var document : criterion.path("documentIds")) confirmedDocuments.add(UUID.fromString(document.asText()));
            }
            onboarding.apply(profile, current, confirmedDocuments);
            current.setStatus("APPROVED");
            current.setDecidedAt(Instant.now());
            current.setDecisionOrigin("AGENT");
            current.setDecisionReason("All required validation criteria passed");
        } else {
            current.setStatus("MANUAL_REVIEW");
        }
        submissions.save(current);
    }

    public static boolean passesPolicy(ObjectMapper json, String result, UUID id, long revision,
            Policy policy, List<UUID> documents) {
        if (!policy.configured() || result == null) return false;
        var node = json.readTree(result);
        if (!id.toString().equals(node.path("submissionId").asText())
                || revision != node.path("revision").asLong()
                || policy.version() != node.path("policyVersion").asInt()
                || !"PASSED".equals(node.path("status").asText())) return false;
        var expected = new HashSet<>(policy.criteria());
        var received = new HashSet<String>();
        var evidence = documents.stream().map(UUID::toString).collect(java.util.stream.Collectors.toSet());
        if (!node.path("criteriaResults").isArray()) return false;
        for (var criterion : node.path("criteriaResults")) {
            if (!received.add(criterion.path("code").asText())
                    || !"PASSED".equals(criterion.path("result").asText())
                    || !criterion.path("documentIds").isArray() || criterion.path("documentIds").isEmpty()) return false;
            for (var document : criterion.path("documentIds")) {
                if (!evidence.contains(document.asText())) return false;
            }
        }
        return received.equals(expected);
    }

    public static boolean passesEvidenceKinds(ObjectMapper json, String result, Map<UUID, String> kinds) {
        var expected = Map.of("IDENTITY_MATCH", "IDENTITY", "REGISTRATION", "REGISTRATION", "NR10", "NR10", "NR35", "NR35", "CERTIFICATES", "CERTIFICATE");
        for (var criterion : json.readTree(result).path("criteriaResults")) {
            String kind = expected.get(criterion.path("code").asText());
            if (kind == null) return false;
            for (var document : criterion.path("documentIds")) {
                try { if (!kind.equals(kinds.get(UUID.fromString(document.asText())))) return false; }
                catch (IllegalArgumentException exception) { return false; }
            }
        }
        return true;
    }

    @Configuration
    @EnableScheduling
    @ConditionalOnProperty(name = "app.professional-validation.enabled", havingValue = "true")
    public static class Scheduling { }
}
