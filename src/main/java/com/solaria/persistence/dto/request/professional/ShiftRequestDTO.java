package com.solaria.persistence.dto.request.professional;

import com.solaria.persistence.domain.enums.professional.DayWeek;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ShiftRequestDTO {

    @NotNull(message = "ID do técnico é obrigatório")
    private UUID technicianId;

    @NotNull(message = "Dia da semana é obrigatório")
    private DayWeek dayWeek;

    @NotNull(message = "Data/hora de início é obrigatória")
    private LocalDateTime startDate;

    @NotNull(message = "Data/hora de término é obrigatória")
    private LocalDateTime endDate;

}
