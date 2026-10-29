package com.solaria.persistence.repository.professional;

import com.solaria.persistence.domain.entity.professional.ProfessionalRegistration;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;


public interface ProfessionalRegistrationRepository extends JpaRepository<ProfessionalRegistration, UUID> {

    List<ProfessionalRegistration> findByTechnicianId(UUID technicianId);

    @EntityGraph(attributePaths = "profession")
    List<ProfessionalRegistration> findByTechnicianIdIn(Collection<UUID> technicianIds);

    List<ProfessionalRegistration> findByTechnicianIdAndTechnicianPersonUserId(UUID technicianId, UUID userId);

    @Query("select u.id from ProfessionalRegistration r left join r.technician t left join t.person p left join p.user u where r.id = :id")
    Optional<UUID> findOwnerUserId(UUID id);

}
