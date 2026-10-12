package com.solaria.persistence.repository.execution;

import com.solaria.persistence.domain.entity.execution.ServiceExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.Optional;


public interface ServiceExecutorRepository extends JpaRepository<ServiceExecutor, UUID> {

    @EntityGraph(attributePaths = {"service.technicalProject.requester.company", "technicianAffiliation.company", "technicianAffiliation.technician"})
    @Query("select entity from ServiceExecutor entity where entity.id = :id")
    Optional<ServiceExecutor> findForManagementById(UUID id);

    @Query("""
            select executor
            from ServiceExecutor executor
            join fetch executor.service service
            join fetch executor.technicianAffiliation affiliation
            join fetch affiliation.technician technician
            join fetch technician.person person
            left join fetch person.user user
            where service.id in :serviceIds
            order by service.id asc, executor.id asc
            """)
    List<ServiceExecutor> findWithCrewByServiceIds(@Param("serviceIds") Collection<UUID> serviceIds);

    @EntityGraph(attributePaths = {"service.technicalProject.requester.company", "technicianAffiliation.company", "technicianAffiliation.technician"})
    List<ServiceExecutor> findByServiceId(UUID serviceId);

    boolean existsByServiceIdAndTechnicianAffiliationId(UUID serviceId, UUID technicianAffiliationId);

    boolean existsByService_IdAndTechnicianAffiliation_Technician_Id(UUID serviceId, UUID technicianId);

    boolean existsByServiceId(UUID serviceId);
}
