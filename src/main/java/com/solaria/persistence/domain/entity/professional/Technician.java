package com.solaria.persistence.domain.entity.professional;

import com.solaria.persistence.domain.enums.professional.TechnicianStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
import com.solaria.persistence.domain.entity.identity.Person;

@Entity
@Table(name = "technician")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Technician {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TechnicianStatus status = TechnicianStatus.UNDER_ANALYSIS;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    private Person person;

    @Column(name = "crea", nullable = false)
    private String crea;

    @Column(name = "slug", length = 160, nullable = false, unique = true)
    private String slug;
}
