package com.solaria.persistence.dto.request.catalog;

import jakarta.validation.constraints.*;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class BrandRequestDTO {
    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 2048)
    private String logoUrl;

    @Size(max = 2048)
    private String referenceUrl;
}
