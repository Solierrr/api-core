package com.solaria.persistence.domain.entity.execution;

import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;


@Entity
@Table(name = "technical_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalService {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technical_project_id", nullable = false)
    private TechnicalProject technicalProject;


    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ServiceStatus status = ServiceStatus.OPEN;


    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledDate;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;


    @Column(name = "accepted_by_user_id")
    private UUID acceptedBy;

    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt;


    @Column(name = "ends_at")
    private OffsetDateTime endDate;
}
