package com.solaria.persistence.dto.response.execution;

public record PersonalTechnicalServiceAddressResponseDTO(
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String zipCode) {
}
