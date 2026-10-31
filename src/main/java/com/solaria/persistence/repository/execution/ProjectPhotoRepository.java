package com.solaria.persistence.repository.execution;

import com.solaria.persistence.domain.entity.execution.ProjectPhoto;

import org.springframework.data.jpa.repository.*;

import java.util.UUID;

public interface ProjectPhotoRepository extends JpaRepository<ProjectPhoto, UUID> {
    @Query(
            "select new"
                + " com.solaria.persistence.dto.response.execution.ProjectPhotoResponse(p.id,p.name,p.mime,p.cover,p.size,p.createdAt)"
                + " from ProjectPhoto p where p.projectId=:projectId order by p.createdAt desc")
    java.util.List<com.solaria.persistence.dto.response.execution.ProjectPhotoResponse> metadata(
            UUID projectId);

    @Query(
            "select new"
                + " com.solaria.persistence.dto.response.execution.ProjectPhotoResponse(p.id,p.name,p.mime,p.cover,p.size,p.createdAt)"
                + " from ProjectPhoto p where p.projectId=:projectId and p.reportId=:reportId order by p.createdAt")
    java.util.List<com.solaria.persistence.dto.response.execution.ProjectPhotoResponse> ofReport(
            UUID projectId, UUID reportId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ProjectPhoto p set p.cover=false where p.projectId=:projectId and p.cover=true")
    void clearCover(UUID projectId);

    java.util.List<ProjectPhoto> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    long countByProjectId(UUID projectId);
}
