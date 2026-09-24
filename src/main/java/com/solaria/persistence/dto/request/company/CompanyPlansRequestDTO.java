package com.solaria.persistence.dto.request.company;

import com.solaria.persistence.domain.enums.company.PlanCycle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class CompanyPlansRequestDTO {

    @NotBlank(message = "Nome do plano é obrigatório")
    private String name;

    @NotNull(message = "Valor do plano é obrigatório")
    private BigDecimal value;

    @NotNull(message = "Ciclo de plano é obrigatório")
    private PlanCycle cycle;

}
