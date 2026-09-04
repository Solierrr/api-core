package com.solaria.persistence.dto.request.professional;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class CertificationRecordRequestDTO {

    private UUID professionalRegistrationId;

    private UUID certificationId;

}
