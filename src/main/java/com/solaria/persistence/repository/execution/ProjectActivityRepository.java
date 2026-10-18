package com.solaria.persistence.repository.execution;

import com.solaria.persistence.domain.entity.execution.ProjectActivity;

import org.springframework.data.jpa.repository.*;

import java.util.UUID;

public interface ProjectActivityRepository extends JpaRepository<ProjectActivity, UUID> {
    boolean existsByTechnicalServiceId(UUID serviceId);

    java.util.List<ProjectActivity> findByProjectIdOrderByStartsAt(UUID projectId);

    boolean existsByProjectIdAndStatusIn(
            UUID projectId,
            java.util.List<com.solaria.persistence.domain.enums.execution.ServiceStatus> statuses);
}
