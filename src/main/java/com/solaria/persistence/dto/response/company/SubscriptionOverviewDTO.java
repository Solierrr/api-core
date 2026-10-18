package com.solaria.persistence.dto.response.company;

import com.solaria.persistence.domain.enums.company.PlanCycle;
import com.solaria.persistence.domain.enums.company.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SubscriptionOverviewDTO(
        UUID id,
        String planName,
        BigDecimal planValue,
        PlanCycle planCycle,
        SubscriptionStatus status,
        Boolean autoRenewal,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        List<ChargeResponseDTO> charges) {
}
