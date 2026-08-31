package com.solaria.persistence.domain.entity.professional;

import com.solaria.persistence.domain.enums.professional.TechnicalAffiliationType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
import com.solaria.persistence.domain.entity.company.Company;

@Entity
@Table(name = "technician_affiliation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianAffiliation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_company")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_technician", nullable = false)
    private Technician technician;

    @Enumerated(EnumType.STRING)
    @Column(name = "affiliation_type", nullable = false)
    private TechnicalAffiliationType affiliationType;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
