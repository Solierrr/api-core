package com.solaria.persistence.dto.request.company;

import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyUpdateRequestDTO {
    @NotBlank
    @Size(max = 120)
    private String tradeName;
    @NotBlank
    @Size(max = 120)
    private String corporateName;
    private UUID addressId;
    @Valid
    private BusinessContactRequestDTO businessContact;
}
