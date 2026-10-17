package com.solaria.persistence.dto.professional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ProfessionalOnboardingContract {
    private ProfessionalOnboardingContract() { }
    public record Draft(long revision, String name, String cpf, LocalDate birthDate,
            String email, String phone, UUID professionId, String council, String registrationNumber,
            LocalDate registrationExpiration, @Size(max = 10) List<UUID> documentIds) { }
    public record Policy(int version, boolean configured, boolean requiresRegistration, String professionName,
            List<String> requiredDocuments, List<String> criteria) { }
    public record Submit(long revision) { }
    public record Decision(boolean approved, long revision, @NotBlank @Size(max = 2000) String reason) { }
    public record Suspension(boolean suspended, @NotBlank @Size(max = 2000) String reason) { }
    public record DocumentInfo(UUID id, String kind, String sha256, String contentType, String fileName) { }
    public record ReviewSummary(UUID id, String name, String registrationNumber) { }
    public record SubmissionInfo(UUID id, long revision, long baseApprovedVersion, String status,
            Draft snapshot, Draft approved, Policy policy, Map<String, Object> validation, Instant createdAt, Instant decidedAt,
            UUID decidedBy, String decisionOrigin, String decisionReason) { }
    public record Profile(long revision, long approvedVersion, boolean suspended,
            Draft draft, Draft approved, List<SubmissionInfo> submissions) { }
    public record Eligibility(UUID authId, UUID userId, UUID technicianId, boolean professional,
            boolean active, boolean registrationComplete, String approvalStatus,
            boolean eligible, List<String> reasons, long approvedVersion, Instant evaluatedAt) { }
}
