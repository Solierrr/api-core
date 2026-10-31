package com.solaria.persistence.domain.entity.execution;

import jakarta.persistence.*;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_report")
@Getter
@Setter
public class ProjectReport {
    @Id private UUID id = UUID.randomUUID();
    @Version private long version;

    @Column(name = "technical_project_id", nullable = false)
    private UUID projectId;

    @Column(name = "author_user_id", nullable = false)
    private UUID authorId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
