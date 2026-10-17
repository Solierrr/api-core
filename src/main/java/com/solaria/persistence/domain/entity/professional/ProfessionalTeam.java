package com.solaria.persistence.domain.entity.professional;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.solaria.persistence.domain.entity.company.Company;

@Entity
@Table(name = "professional_team")
@Getter
@Setter
public class ProfessionalTeam {
    @Id private UUID id = UUID.randomUUID();
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "company_id", nullable = false) private Company company;
    @Column(nullable = false, length = 100) private String name;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "responsible_id", nullable = false) private Technician responsible;
    @Column(nullable = false) private boolean active = true;
}
