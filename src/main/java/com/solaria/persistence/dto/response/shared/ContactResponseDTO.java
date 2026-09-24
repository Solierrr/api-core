package com.solaria.persistence.dto.response.shared;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class ContactResponseDTO {

    private UUID id;
    private String email;
    private String phone;

}
