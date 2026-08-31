package com.solaria.persistence.dto.response.professional;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CertificationRecordResponseDTO {

    private UUID id;
    private UUID professionalRegistrationId;
    private UUID certificationId;

}
