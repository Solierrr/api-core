package com.solaria.persistence.domain.entity.execution;

import com.solaria.persistence.domain.enums.execution.*;

import jakarta.persistence.*;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Only the hash of the token is stored; the link itself is shown once, when it is created. */
@Entity
@Table(name = "project_invite_link")
@Getter
@Setter
public class ProjectInviteLink {
    @Id private UUID id = UUID.randomUUID();
    @Version private long version;

    @Column(name = "technical_project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false, updatable = false)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectRole role;

    private String functionName;
    private Integer maxUses;

    @Column(nullable = false)
    private int useCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectInviteLinkStatus status = ProjectInviteLinkStatus.ACTIVE;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdBy;

    private OffsetDateTime revokedAt;
}
