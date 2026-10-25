package com.solaria.persistence.dto.response.professional;

import com.solaria.persistence.domain.enums.professional.DayWeek;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShiftResponseDTO {

    private UUID id;
    private UUID technicianId;
    private DayWeek dayWeek;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;

}
