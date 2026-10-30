package com.solaria.persistence.domain.entity.professional;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.execution.TechnicalService;


@Entity
@Table(name = "professional_review",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_professional_review_reviewer_technician_service",
                columnNames = {"reviewer_user_id", "technician_id", "technical_service_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician professional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_user_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technical_service_id", nullable = false)
    private TechnicalService service;

    @Column(name = "rating", nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
