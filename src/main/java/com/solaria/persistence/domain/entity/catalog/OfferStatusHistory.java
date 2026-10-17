package com.solaria.persistence.domain.entity.catalog;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.solaria.persistence.domain.enums.catalog.OfferStatus;

@Entity
@Table(name = "offer_status_history")
@Getter
@Setter
public class OfferStatusHistory {
    @Id private UUID id = UUID.randomUUID();
    @Column(name = "offer_id", nullable = false) private UUID offerId;
    @Enumerated(EnumType.STRING) @Column(name = "previous_status", nullable = false, length = 16) private OfferStatus previousStatus;
    @Enumerated(EnumType.STRING) @Column(name = "new_status", nullable = false, length = 16) private OfferStatus newStatus;
    @Column(name = "actor_id", nullable = false) private UUID actorId;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
}
