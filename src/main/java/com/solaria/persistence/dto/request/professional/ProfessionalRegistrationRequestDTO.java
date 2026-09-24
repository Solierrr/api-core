package com.solaria.persistence.dto.request.professional;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
public class ProfessionalRegistrationRequestDTO {

    private UUID technicianId;

    private UUID professionId;

    @Size(max = 60)
    private String council;

    @Size(max = 30)
    private String number;

    private LocalDateTime expirationDate;

}
