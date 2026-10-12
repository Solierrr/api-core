package com.solaria.persistence.repository.execution;

import com.solaria.persistence.domain.entity.execution.TechnicalService;
import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface TechnicalServiceRepository extends JpaRepository<TechnicalService, UUID> {

    @EntityGraph(attributePaths = {"technicalProject.requester.company"})
    @Query("select entity from TechnicalService entity where entity.id = :id")
    Optional<TechnicalService> findForManagementById(UUID id);

    @EntityGraph(attributePaths = {"technicalProject.requester.company", "technicalProject.localUnit.address"})
    @Query("select entity from TechnicalService entity where entity.id = :id")
    Optional<TechnicalService> findForMobileById(UUID id);

    @Query("""
            select distinct service
            from TechnicalService service
            join fetch service.technicalProject project
            left join fetch project.requester requester
            left join fetch requester.company requesterCompany
            left join fetch project.localUnit localUnit
            left join fetch localUnit.address address
            where exists (
                select executor.id
                from ServiceExecutor executor
                join executor.technicianAffiliation affiliation
                join affiliation.technician technician
                join technician.person person
                where executor.service = service
                    and person.user.id = :userId
                    and (service.status in :historicalStatuses or affiliation.active = true)
            )
            order by service.createdAt desc, service.id asc
            """)
    List<TechnicalService> findAssignedToUserInMobileScope(
            @Param("userId") UUID userId,
            @Param("historicalStatuses") Collection<ServiceStatus> historicalStatuses);

    @EntityGraph(attributePaths = {"technicalProject.requester.company"})
    List<TechnicalService> findByTechnicalProject_Requester_Company_Id(UUID companyId);

    Optional<TechnicalService> findByIdAndTechnicalProject_Requester_Company_Id(UUID id, UUID companyId);

    @EntityGraph(attributePaths = {"technicalProject.requester.company"})
    List<TechnicalService> findByTechnicalProjectId(UUID technicalProjectId);

    boolean existsByTechnicalProjectId(UUID technicalProjectId);

    boolean existsByTechnicalProjectIdAndStatusIn(UUID technicalProjectId, List<ServiceStatus> statuses);
}
