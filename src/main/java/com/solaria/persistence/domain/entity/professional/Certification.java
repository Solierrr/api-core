package com.solaria.persistence.domain.entity.professional;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;


@Entity
@Table(name = "certification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "issuer", length = 100)
    private String issuer;

    @Column(name = "valid_until_at")
    private OffsetDateTime validity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
