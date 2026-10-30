package com.solaria.persistence.repository.professional;

import com.solaria.persistence.domain.entity.professional.TechnicianAffiliation;
import com.solaria.persistence.dto.response.professional.AccreditedTechnicianResponseDTO;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;


public interface TechnicianAffiliationRepository extends JpaRepository<TechnicianAffiliation, UUID> {

    @EntityGraph(attributePaths = {"technician", "company"})
    List<TechnicianAffiliation> findByCompanyId(UUID companyId);


    boolean existsByTechnicianIdAndActiveTrue(UUID technicianId);

    @EntityGraph(attributePaths = {"technician", "company"})
    List<TechnicianAffiliation> findByTechnicianId(UUID technicianId);

    boolean existsByCompanyId(UUID companyId);

    @Query("select new com.solaria.persistence.dto.response.professional.AccreditedTechnicianResponseDTO("
            + "p.name, c.tradeName, a.affiliationType, prof.name, r.council, r.number, r.expirationDate, addr.city) "
            + "from TechnicianAffiliation a join a.technician t join t.person p join a.company c join c.address addr "
            + "left join ProfessionalRegistration r on r.technician = t left join r.profession prof "
            + "where t.status = com.solaria.persistence.domain.enums.professional.TechnicianStatus.APPROVED and a.active = true "
            + "and (:anyProfession = true or lower(prof.name) like :profession) "
            + "and (:anyCity = true or lower(addr.city) like :city) "
            + "order by p.name, a.id, r.id")
    List<AccreditedTechnicianResponseDTO> findAccredited(@Param("anyProfession") boolean anyProfession,
            @Param("profession") String profession, @Param("anyCity") boolean anyCity,
            @Param("city") String city, Limit limit);

}
