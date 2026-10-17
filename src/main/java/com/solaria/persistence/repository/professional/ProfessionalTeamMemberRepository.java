package com.solaria.persistence.repository.professional;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import com.solaria.persistence.domain.entity.professional.ProfessionalTeamMember;

public interface ProfessionalTeamMemberRepository extends JpaRepository<ProfessionalTeamMember, UUID> {
    List<ProfessionalTeamMember> findByTeamId(UUID teamId);
    void deleteByTeamId(UUID teamId);
}
