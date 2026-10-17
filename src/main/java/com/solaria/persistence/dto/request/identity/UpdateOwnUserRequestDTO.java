package com.solaria.persistence.dto.request.identity;

import com.solaria.persistence.util.RegexValidator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateOwnUserRequestDTO(
        @NotBlank @Pattern(regexp = RegexValidator.USERNAME_REGEX) String username) {
}
