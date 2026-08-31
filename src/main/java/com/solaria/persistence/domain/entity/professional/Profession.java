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

    @Column(name = "accept_emergency_call", nullable = false)
    private Boolean accept_emergency_call;

    @Column(name = "requires_registration")
    private Boolean requiresRegistration;
}
