package com.solaria.persistence.dto.response.shared;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponseDTO {

    private UUID id;
    private String state;
    private String city;
    private String neighborhood;
    private String zipCode;
    private String street;
    private String number;

}
