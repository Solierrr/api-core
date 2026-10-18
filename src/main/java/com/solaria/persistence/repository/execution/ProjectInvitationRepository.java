package com.solaria.persistence.repository.execution;

import com.solaria.persistence.domain.entity.execution.ProjectInvitation;

import org.springframework.data.jpa.repository.*;

import java.util.UUID;

public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, UUID> {
    java.util.List<ProjectInvitation> findByProjectIdOrderByCreatedAtDesc(UUID id);

    @Query(
            "select i from ProjectInvitation i where"
                + " i.status=com.solaria.persistence.domain.enums.execution.ProjectInvitationStatus.PENDING"
                + " and (i.recipientAuthId=:authId or lower(i.email)=:email) order by i.createdAt"
                + " desc")
    java.util.List<ProjectInvitation> findPendingFor(UUID authId, String email);

    @Query(
            "select i from ProjectInvitation i where i.emailState='PENDING' and"
                + " i.status=com.solaria.persistence.domain.enums.execution.ProjectInvitationStatus.PENDING"
                + " and i.expiresAt>:now and (i.nextEmailAttemptAt is null or"
                + " i.nextEmailAttemptAt<=:now)")
    java.util.List<ProjectInvitation> pendingEmail(
            java.time.OffsetDateTime now, org.springframework.data.domain.Pageable page);
}
