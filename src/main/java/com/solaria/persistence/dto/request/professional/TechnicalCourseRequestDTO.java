package com.solaria.persistence.dto.request.professional;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class TechnicalCourseRequestDTO {

    private UUID companyId;

    @Size(max = 30)
    private String title;

    private String information;

    private String link;

}
