package com.solaria.persistence.domain.entity.execution;

import com.solaria.persistence.domain.enums.execution.*;

import jakarta.persistence.*;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_member")
@Getter
@Setter
public class ProjectMember {
    @Id private UUID id = UUID.randomUUID();
    @Version private long version;

    @Column(nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectRole role;

    @Column(name = "function_name")
    private String functionName;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private OffsetDateTime joinedAt = OffsetDateTime.now();
}
