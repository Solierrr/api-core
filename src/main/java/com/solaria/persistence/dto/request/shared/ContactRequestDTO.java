package com.solaria.persistence.dto.request.shared;

import com.solaria.persistence.util.RegexValidator;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ContactRequestDTO {

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 9)
    @Pattern(regexp = RegexValidator.PHONE_REGEX, message = "Telefone inválido")
    private String phone;

}
