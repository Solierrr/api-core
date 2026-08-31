package com.solaria.persistence.domain.entity.professional;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "professional_registration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_technician")
    private Technician technician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_profession")
    private Profession profession;

    @Column(name = "council", length = 60)
    private String council;

    @Column(name = "number", length = 30)
    private String number;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;
}
