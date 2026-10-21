package com.solaria.persistence.dto.request.company;

import com.solaria.persistence.util.RegexValidator;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.Valid;
import com.solaria.persistence.dto.request.shared.AddressRequestDTO;
import com.solaria.persistence.domain.enums.company.CompanyType;

@Setter
@Getter
public class CompanyCreateRequestDTO {

    @Valid
    private AddressRequestDTO address;
    @NotNull(message = "Tipo da empresa é obrigatório")
    private CompanyType type;

    @Valid
    private BusinessContactRequestDTO businessContact;

    @NotBlank(message = "CNPJ é obrigatório")
    @Size(max = 14)
    @Pattern(regexp = RegexValidator.CNPJ_REGEX, message = "CNPJ inválido")
    private String cnpj;

    @NotBlank(message = "Nome Fantasia da empresa é obrigatório")
    @Size(max = 120)
    private String tradeName;

    @NotBlank(message = "Razão Social da empresa é obrigatória")
    @Size(max = 120)
    private String corporateName;

}
