package com.solaria.persistence.repository.execution;

import com.solaria.persistence.domain.entity.execution.ProjectInviteLink;

import org.springframework.data.jpa.repository.*;

import java.time.OffsetDateTime;
import java.util.*;

public interface ProjectInviteLinkRepository extends JpaRepository<ProjectInviteLink, UUID> {
    List<ProjectInviteLink> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    Optional<ProjectInviteLink> findByIdAndProjectId(UUID id, UUID projectId);

    Optional<ProjectInviteLink> findByTokenHash(String tokenHash);

    /** Consumes one use atomically, so concurrent redemptions never pass the limit. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            "update ProjectInviteLink l set l.useCount = l.useCount + 1 where l.id = :id and"
                + " l.status = com.solaria.persistence.domain.enums.execution.ProjectInviteLinkStatus.ACTIVE"
                + " and l.expiresAt > :now and (l.maxUses is null or l.useCount < l.maxUses)")
    int consume(UUID id, OffsetDateTime now);
}
