package com.solaria.persistence.dto.response.company;

import com.solaria.persistence.domain.enums.company.PlanCycle;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CompanyPlansResponseDTO {

    private UUID id;
    private String name;
    private BigDecimal value;
    private PlanCycle cycle;

}
