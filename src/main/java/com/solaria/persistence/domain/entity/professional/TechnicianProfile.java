package com.solaria.persistence.domain.entity.professional;

import com.solaria.persistence.domain.enums.professional.TechnicianSpecialty;

import jakarta.persistence.*;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * What the technician fills in after signing up: where they work, how far they go, experience and a short
 * presentation. Saving is a draft; {@code completedAt} is set only when the required data is present.
 */
@Entity
@Table(name = "technician_profile")
@Getter
@Setter
public class TechnicianProfile {
    @Id
    @Column(name = "technician_id")
    private UUID technicianId;

    @Version private long version;

    @Column(length = 500)
    private String bio;

    private Integer yearsExperience;
    private Integer serviceRadiusKm;

    private String zipCode;
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;

    @ElementCollection
    @CollectionTable(name = "technician_specialty", joinColumns = @JoinColumn(name = "technician_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "specialty", nullable = false)
    private Set<TechnicianSpecialty> specialties = EnumSet.noneOf(TechnicianSpecialty.class);

    /** What the technician does when they choose "other"; only kept while that specialty is selected. */
    private String otherSpecialty;

    private OffsetDateTime completedAt;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
