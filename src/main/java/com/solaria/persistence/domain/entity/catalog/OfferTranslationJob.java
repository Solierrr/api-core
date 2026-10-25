package com.solaria.persistence.domain.entity.catalog;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "offer_translation_job", uniqueConstraints = @UniqueConstraint(name = "uq_offer_translation_job_offer_source_offer_translation", columnNames = {"offer_id", "source_offer_translation_id"}))
@Getter
@Setter
public class OfferTranslationJob {
    @Id private UUID id = UUID.randomUUID();
    @Column(name = "offer_id", nullable = false) private UUID offerId;
    @Column(name = "source_offer_translation_id", nullable = false) private UUID sourceTranslationId;
    @Column(nullable = false, length = 20) private String status = "QUEUED";
    @Column(nullable = false) private int attempts;
    @Column(name = "available_at", nullable = false) private Instant availableAt = Instant.now();
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    @Column(name = "claim_token") private UUID claimToken;
    @Column(name = "error_code", length = 40) private String errorCode;
}
