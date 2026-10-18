package com.solaria.persistence.domain.entity.execution;

import com.solaria.persistence.domain.enums.execution.*;

import jakarta.persistence.*;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_activity")
@Getter
@Setter
public class ProjectActivity {
    @Id private UUID id = UUID.randomUUID();
    @Version private long version;

    @Column(nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectActivityType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceStatus status = ServiceStatus.OPEN;

    @Column(nullable = false)
    private OffsetDateTime startsAt;

    private OffsetDateTime endsAt;

    @Column(nullable = false)
    private boolean onsite;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.ARRAY)
    @Column(nullable = false)
    private java.util.List<UUID> assigneeIds = new java.util.ArrayList<>();

    private UUID technicalServiceId;
}
