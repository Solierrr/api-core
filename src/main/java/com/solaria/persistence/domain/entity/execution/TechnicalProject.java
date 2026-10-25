package com.solaria.persistence.domain.entity.execution;

import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.solaria.persistence.domain.entity.unit.LocalUnit;


@Entity
@Table(name = "technical_project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalProject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private Requester requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_unit_id")
    private LocalUnit localUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ServiceStatus status;

    @Column(name = "starts_at")
    private OffsetDateTime startDate;

    @Column(name = "ends_at")
    private OffsetDateTime endDate;

    @Column(name="name",nullable=false,length=160) private String name="Projeto técnico";
    @Column(columnDefinition="text") private String description;
    private UUID ownerCompanyId;
    private UUID ownerUserId;
    @Column(name = "created_by_user_id") private UUID createdBy;
    @Column(name = "is_archived", nullable=false) private boolean archived;
    @Column(nullable=false) private OffsetDateTime createdAt=OffsetDateTime.now();
    @Embedded private ProjectLocation location;
    @Version private long version;
}
