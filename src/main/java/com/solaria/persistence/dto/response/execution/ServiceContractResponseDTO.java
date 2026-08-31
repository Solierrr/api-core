package com.solaria.persistence.dto.response.execution;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
public class ServiceContractResponseDTO {

    private UUID id;
    private UUID serviceId;
    private String warranty;
    private LocalDate deliveryDeadline;
    private Boolean insurance;
    private Boolean utilityApproval;

}
