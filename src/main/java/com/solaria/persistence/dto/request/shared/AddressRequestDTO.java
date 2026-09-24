package com.solaria.persistence.dto.request.shared;

import com.solaria.persistence.util.RegexValidator;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddressRequestDTO {

    @NotBlank(message = "Estado é obrigatório")
    @Size(max = 2)
    private String state;

    @NotBlank(message = "Cidade é obrigatória")
    private String city;

    private String neighborhood;

    @NotBlank(message = "CEP é obrigatório")
    @Size(max = 8)
    @Pattern(regexp = RegexValidator.ZIP_CODE_REGEX, message = "CEP inválido")
    private String zipCode;

    @NotBlank(message = "Rua é obrigatória")
    private String street;

    @Size(max = 10)
    private String number;

}
