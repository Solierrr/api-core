package com.solaria.persistence.repository.execution;

import com.solaria.persistence.domain.entity.execution.ProjectHistory;

import org.springframework.data.jpa.repository.*;

import java.util.UUID;

public interface ProjectHistoryRepository extends JpaRepository<ProjectHistory, UUID> {
    java.util.List<ProjectHistory> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
