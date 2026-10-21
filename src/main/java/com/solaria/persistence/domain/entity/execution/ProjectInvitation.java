package com.solaria.persistence.domain.entity.execution;

import com.solaria.persistence.domain.enums.execution.*;

import jakarta.persistence.*;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_invitation")
@Getter
@Setter
public class ProjectInvitation {
    @Id private UUID id = UUID.randomUUID();
    @Version private long version;

    @Column(nullable = false)
    private UUID projectId;

    private UUID recipientAuthId;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectRole role;

    private String functionName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectInvitationStatus status = ProjectInvitationStatus.PENDING;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(nullable = false)
    private UUID createdBy;

    @Column(nullable = false)
    private String locale = "pt-BR";

    @Column(nullable = false)
    private String emailState = "PENDING";

    @Column(nullable = false)
    private int emailAttempts;

    private OffsetDateTime nextEmailAttemptAt;
    private OffsetDateTime respondedAt;
}
