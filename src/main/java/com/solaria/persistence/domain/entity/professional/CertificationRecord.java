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
    @JoinColumn(name = "fk_professional_registration")
    private ProfessionalRegistration professionalRegistration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_certification")
    private Certification certification;
}
