package com.solaria.persistence.repository.execution;

import com.solaria.persistence.domain.entity.execution.ProjectReport;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface ProjectReportRepository extends JpaRepository<ProjectReport, UUID> {
    List<ProjectReport> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
