package com.solaria.persistence.dto.response.execution;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import com.solaria.persistence.dto.response.professional.TechnicianAffiliationResponseDTO;

@Setter
@Getter
public class ServiceExecutorResponseDTO {

    private UUID id;
    private UUID serviceId;
    private TechnicianAffiliationResponseDTO technicianAffiliation;
    private String position;

}
