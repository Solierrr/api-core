package com.solaria.persistence.repository.professional;

import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import com.solaria.persistence.domain.entity.professional.ProfessionalTeam;

public interface ProfessionalTeamRepository extends JpaRepository<ProfessionalTeam, UUID> {
    List<ProfessionalTeam> findByCompanyId(UUID companyId);
    List<ProfessionalTeam> findByActiveTrue();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from ProfessionalTeam t where t.id = :id")
    Optional<ProfessionalTeam> lockById(UUID id);
}
