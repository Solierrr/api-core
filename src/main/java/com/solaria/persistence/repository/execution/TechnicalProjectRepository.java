package com.solaria.persistence.repository.execution;

import com.solaria.persistence.domain.entity.execution.TechnicalProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface TechnicalProjectRepository extends JpaRepository<TechnicalProject, UUID> {

    List<TechnicalProject> findByRequester_Company_Id(UUID companyId);

    Optional<TechnicalProject> findByIdAndRequester_Company_Id(UUID id, UUID companyId);

    List<TechnicalProject> findByRequesterId(UUID requesterId);

    boolean existsByRequesterId(UUID requesterId);
}
