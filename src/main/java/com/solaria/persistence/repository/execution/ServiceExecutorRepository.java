package com.solaria.persistence.repository.execution;

import com.solaria.persistence.domain.entity.execution.ServiceExecutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ServiceExecutorRepository extends JpaRepository<ServiceExecutor, UUID> {


    List<ServiceExecutor> findByServiceId(UUID serviceId);

    boolean existsByServiceIdAndTechnicianAffiliationId(UUID serviceId, UUID technicianAffiliationId);

    boolean existsByService_IdAndTechnicianAffiliation_Technician_Id(UUID serviceId, UUID technicianId);

    boolean existsByServiceId(UUID serviceId);
}
