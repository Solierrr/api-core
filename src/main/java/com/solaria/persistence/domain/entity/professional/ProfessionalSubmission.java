package com.solaria.persistence.domain.entity.professional;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "professional_submission")
@Getter
@Setter
public class ProfessionalSubmission {
    @Id
    private UUID id;
    @Column(name = "onboarding_id", nullable = false)
    private UUID onboardingId;
    @Column(nullable = false)
    private long revision;
    @Column(name = "base_approved_version", nullable = false)
    private long baseApprovedVersion;
    @Column(name = "snapshot_json", columnDefinition = "text", nullable = false)
    private String snapshotJson;
    @Column(name = "policy_json", columnDefinition = "text", nullable = false)
    private String policyJson;
    @Column(nullable = false, length = 30)
    private String status = "QUEUED";
    @Column(name = "result_json", columnDefinition = "text")
    private String resultJson;
    @Column(nullable = false)
    private int attempts;
    @Column(name = "available_at", nullable = false)
    private Instant availableAt = Instant.now();
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "decided_at")
    private Instant decidedAt;
    @Column(name = "decided_by")
    private UUID decidedBy;
    @Column(name = "decision_origin", length = 10)
    private String decisionOrigin;
    @Column(name = "decision_reason", columnDefinition = "text")
    private String decisionReason;
}
