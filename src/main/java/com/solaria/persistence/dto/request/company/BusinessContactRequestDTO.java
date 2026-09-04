package com.solaria.persistence.dto.request.company;

import com.solaria.persistence.util.RegexValidator;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessContactRequestDTO {

    @NotBlank(message = "Email da empresa é obrigatório")
    @Email
    @Size(max = 100)
    private String companyEmail;

    @Size(max = 9)
    @Pattern(regexp = RegexValidator.PHONE_REGEX, message = "Telefone inválido")
    private String phone;

    @Pattern(regexp = RegexValidator.URL_REGEX, message = "Website inválido")
    private String website;

}
