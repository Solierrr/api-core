package com.solaria.persistence.dto.request;

import com.solaria.persistence.util.RegexValidator;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserRequestDTO {

    @NotNull(message = "ID de autenticação é obrigatório")
    private UUID authId;

    @NotNull(message = "Username é obrigatório")
    @Pattern(regexp = RegexValidator.USERNAME_REGEX, message = "Username inválido")
    private String username;

    @Pattern(regexp = RegexValidator.URL_REGEX, message = "Avatar inválido")
    private String avatar;

    @Pattern(regexp = RegexValidator.URL_REGEX, message = "Banner inválido")
    private String banner;

}
