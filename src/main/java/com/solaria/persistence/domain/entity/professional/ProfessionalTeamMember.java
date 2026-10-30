package com.solaria.persistence.domain.entity.professional;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "professional_team_member", uniqueConstraints = @UniqueConstraint(name = "uq_professional_team_member_professional_team_technician", columnNames = {"professional_team_id","technician_id"}))
@Getter
@Setter
public class ProfessionalTeamMember {
    @Id private UUID id = UUID.randomUUID();
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "professional_team_id", nullable = false) private ProfessionalTeam team;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "technician_id", nullable = false) private Technician technician;
}
