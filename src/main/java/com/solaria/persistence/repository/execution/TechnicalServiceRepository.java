package com.solaria.persistence.repository.execution;

import com.solaria.persistence.domain.entity.execution.TechnicalService;
import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface TechnicalServiceRepository extends JpaRepository<TechnicalService, UUID> {

    List<TechnicalService> findByTechnicalProject_Requester_Company_Id(UUID companyId);

    Optional<TechnicalService> findByIdAndTechnicalProject_Requester_Company_Id(UUID id, UUID companyId);

    List<TechnicalService> findByTechnicalProjectId(UUID technicalProjectId);

    boolean existsByTechnicalProjectId(UUID technicalProjectId);

    boolean existsByTechnicalProjectIdAndStatusIn(UUID technicalProjectId, List<ServiceStatus> statuses);
}
