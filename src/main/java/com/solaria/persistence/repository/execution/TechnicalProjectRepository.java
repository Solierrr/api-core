package com.solaria.persistence.repository.execution;

import com.solaria.persistence.domain.entity.execution.TechnicalProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface TechnicalProjectRepository extends JpaRepository<TechnicalProject, UUID> {

    @EntityGraph(attributePaths = {"requester.company", "localUnit"})
    @Query("select entity from TechnicalProject entity where entity.id = :id")
    Optional<TechnicalProject> findForManagementById(UUID id);

    @EntityGraph(attributePaths = {"requester.company", "localUnit"})
    List<TechnicalProject> findByRequester_Company_Id(UUID companyId);

    Optional<TechnicalProject> findByIdAndRequester_Company_Id(UUID id, UUID companyId);

    @EntityGraph(attributePaths = {"requester.company", "localUnit"})
    List<TechnicalProject> findByRequesterId(UUID requesterId);

    boolean existsByRequesterId(UUID requesterId);
}
