package com.solaria.persistence.dto.request.professional;

import com.solaria.persistence.domain.enums.professional.TechnicianSpecialty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.Set;

/** The whole draft: what is not sent is cleared, so the client always sends the full state of the form. */
public record TechnicianProfileRequest(
        @Size(max = 500) String bio,
        @Min(0) @Max(60) Integer yearsExperience,
        @Min(1) @Max(500) Integer serviceRadiusKm,
        @Valid Address address,
        Set<TechnicianSpecialty> specialties) {

    public record Address(
            @Pattern(regexp = "^\\d{5}-?\\d{3}$") String zipCode,
            @Size(max = 160) String street,
            @Size(max = 20) String number,
            @Size(max = 120) String complement,
            @Size(max = 120) String neighborhood,
            @Size(max = 120) String city,
            @Pattern(regexp = "^[A-Za-z]{2}$") String state) {}
}
