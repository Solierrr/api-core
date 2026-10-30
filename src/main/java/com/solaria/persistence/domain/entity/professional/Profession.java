package com.solaria.persistence.domain.entity.professional;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "profession")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Profession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "is_emergency_call_accepted", nullable = false)
    private Boolean accept_emergency_call;

    @Column(name = "is_registration_required")
    private Boolean requiresRegistration;

    @Column(name = "is_nr10_required")
    private Boolean requiresNr10;

    @Column(name = "is_nr35_required")
    private Boolean requiresNr35;
}
