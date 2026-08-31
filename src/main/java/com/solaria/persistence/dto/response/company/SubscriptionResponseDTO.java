package com.solaria.persistence.dto.response.company;

import com.solaria.persistence.domain.enums.company.SubscriptionStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Setter
@Getter
public class SubscriptionResponseDTO {

    private UUID id;
    private UUID supplierId;
    private CompanyPlansResponseDTO plan;
    private SubscriptionStatus status;
    private Boolean autoRenewal;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;

}
