package com.solaria.persistence.domain.entity.execution;

import com.solaria.persistence.domain.enums.execution.*;

import jakarta.persistence.*;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_history")
@Getter
@Setter
public class ProjectHistory {
    @Id private UUID id = UUID.randomUUID();
    @Version private long version;

    @Column(name = "technical_project_id", nullable = false)
    private UUID projectId;

    @Column(name = "actor_user_id", nullable = false)
    private UUID actorId;

    @Column(nullable = false)
    private String action;

    @Column(columnDefinition = "text", nullable = false)
    private String detail;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
