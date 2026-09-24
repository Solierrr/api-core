package com.solaria.persistence.dto.patch.execution;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class ServiceScheduleDTO {

    @NotNull(message = "Data agendada é obrigatória")
    private OffsetDateTime scheduledDate;

}
