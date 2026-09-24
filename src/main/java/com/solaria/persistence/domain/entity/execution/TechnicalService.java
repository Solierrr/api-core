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
    @JoinColumn(name = "fk_technical_project", nullable = false)
    private TechnicalProject technicalProject;


    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ServiceStatus status = ServiceStatus.OPEN;


    @Column(name = "scheduled_date")
    private OffsetDateTime scheduledDate;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;


    @Column(name = "accepted_by")
    private UUID acceptedBy;

    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt;


    @Column(name = "end_date")
    private OffsetDateTime endDate;
}
