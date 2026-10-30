package com.solaria.persistence.domain.entity.professional;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "certification_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_registration_id")
    private ProfessionalRegistration professionalRegistration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_id")
    private Certification certification;
}
