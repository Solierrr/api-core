package com.solaria.persistence.repository.execution;

import com.solaria.persistence.domain.entity.execution.ProjectMember;

import org.springframework.data.jpa.repository.*;

import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    java.util.List<ProjectMember> findByProjectIdAndActiveTrue(UUID projectId);

    java.util.Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);
}
