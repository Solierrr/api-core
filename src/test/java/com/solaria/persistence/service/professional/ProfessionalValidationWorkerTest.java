package com.solaria.persistence.service.professional;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.solaria.persistence.dto.professional.ProfessionalOnboardingContract.Policy;
import tools.jackson.databind.ObjectMapper;

class ProfessionalValidationWorkerTest {
    private final ObjectMapper json = new ObjectMapper();
    private final UUID id = UUID.randomUUID();
    private final UUID document = UUID.randomUUID();
    private final Policy policy = new Policy(1, true, false, "Instalador", List.of("IDENTITY"), List.of("IDENTITY_MATCH"));

    private String result(String code, String evidence, long revision) {
        return json.writeValueAsString(Map.of("submissionId", id, "revision", revision, "policyVersion", 1,
            "status", "PASSED", "criteriaResults", List.of(Map.of("code", code, "result", "PASSED", "documentIds", List.of(evidence)))));
    }

    @Test
    void onlyMatchingVersionAndCompleteEvidenceCanApprove() {
        assertTrue(ProfessionalValidationWorker.passesPolicy(json, result("IDENTITY_MATCH", document.toString(), 2), id, 2, policy, List.of(document)));
        assertFalse(ProfessionalValidationWorker.passesPolicy(json, result("IDENTITY_MATCH", document.toString(), 1), id, 2, policy, List.of(document)));
        assertFalse(ProfessionalValidationWorker.passesPolicy(json, result("NR10", document.toString(), 2), id, 2, policy, List.of(document)));
        assertFalse(ProfessionalValidationWorker.passesPolicy(json, result("IDENTITY_MATCH", UUID.randomUUID().toString(), 2), id, 2, policy, List.of(document)));
        assertFalse(ProfessionalValidationWorker.passesPolicy(json, result("IDENTITY_MATCH", document.toString(), 2), id, 2,
            new Policy(1, false, false, "Instalador", List.of("IDENTITY"), List.of("IDENTITY_MATCH")), List.of(document)));
    }

    @Test
    void identityEvidenceCannotSupportNrApproval() {
        var result = result("NR10", document.toString(), 2);
        assertFalse(ProfessionalValidationWorker.passesEvidenceKinds(json, result, Map.of(document, "IDENTITY")));
        assertTrue(ProfessionalValidationWorker.passesEvidenceKinds(json, result, Map.of(document, "NR10")));
    }

    @Test
    void repeatedResultsCannotApprove() {
        var node = json.readTree(result("IDENTITY_MATCH", document.toString(), 2));
        var duplicated = json.writeValueAsString(Map.of("submissionId", id, "revision", 2, "policyVersion", 1,
            "status", "PASSED", "criteriaResults", List.of(node.path("criteriaResults").get(0), node.path("criteriaResults").get(0))));
        assertFalse(ProfessionalValidationWorker.passesPolicy(json, duplicated, id, 2, policy, List.of(document)));
    }
}
