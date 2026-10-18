package com.solaria.persistence.domain.entity.catalog;

import com.solaria.persistence.domain.enums.catalog.*;

import jakarta.persistence.*;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "brand_request")
@Getter
@Setter
@NoArgsConstructor
public class BrandRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_brand", nullable = false)
    private Brand brand;

    @Column(name = "fk_requester_company")
    private UUID requesterCompanyId;

    @Column(name = "fk_requester_user")
    private UUID requesterUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BrandRequestKind kind;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BrandRequestStatus status = BrandRequestStatus.PENDING;

    @Column(name = "proposed_name", nullable = false, length = 255)
    private String proposedName;

    @Column(name = "proposed_logo_url", length = 2048)
    private String proposedLogoUrl;

    @Column(name = "proposed_reference_url", length = 2048)
    private String proposedReferenceUrl;

    @Column(name = "fk_result_brand")
    private UUID resultBrandId;

    @Column(name = "decision_reason", length = 2000)
    private String decisionReason;

    @Column(name = "fk_decided_by")
    private UUID decidedBy;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Version private long revision;
}
