package com.solaria.persistence.dto.request.professional;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProfessionRequestDTO {

    @Size(max = 100)
    private String name;

    private Boolean requiresRegistration;

}
