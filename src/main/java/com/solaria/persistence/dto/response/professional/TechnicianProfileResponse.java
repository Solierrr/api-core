package com.solaria.persistence.dto.response.professional;

import com.solaria.persistence.domain.enums.professional.TechnicianSpecialty;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * {@code missing} lists what still blocks completion ({@code ADDRESS}, {@code SPECIALTIES}); {@code progress} is the
 * share of the five profile items already filled in, 0 to 100.
 */
public record TechnicianProfileResponse(
        UUID technicianId,
        String bio,
        Integer yearsExperience,
        Integer serviceRadiusKm,
        Address address,
        Set<TechnicianSpecialty> specialties,
        boolean completed,
        OffsetDateTime completedAt,
        int progress,
        List<String> missing) {

    public record Address(
            String zipCode,
            String street,
            String number,
            String complement,
            String neighborhood,
            String city,
            String state) {}
}
