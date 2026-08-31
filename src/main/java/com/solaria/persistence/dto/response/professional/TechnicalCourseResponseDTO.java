package com.solaria.persistence.dto.response.professional;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TechnicalCourseResponseDTO {

    private UUID id;
    private UUID companyId;
    private String title;
    private String information;
    private String link;

}
