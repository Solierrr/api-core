package com.solaria.persistence.repository.professional;

import com.solaria.persistence.domain.entity.professional.TechnicianAffiliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.UUID;


public interface TechnicianAffiliationRepository extends JpaRepository<TechnicianAffiliation, UUID> {

    @EntityGraph(attributePaths = {"technician", "company"})
    List<TechnicianAffiliation> findByCompanyId(UUID companyId);


    boolean existsByTechnicianIdAndActiveTrue(UUID technicianId);

    @EntityGraph(attributePaths = {"technician", "company"})
    List<TechnicianAffiliation> findByTechnicianId(UUID technicianId);

    boolean existsByCompanyId(UUID companyId);

}
