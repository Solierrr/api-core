package com.solaria.persistence.dto.response.company;

import com.solaria.persistence.domain.enums.company.BillingStatus;
import com.solaria.persistence.domain.enums.company.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Setter
@Getter
public class ChargeResponseDTO {

    private UUID id;
    private UUID subscriptionId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private BillingStatus status;
    private LocalDate dueDate;
    private OffsetDateTime paymentDate;

}
