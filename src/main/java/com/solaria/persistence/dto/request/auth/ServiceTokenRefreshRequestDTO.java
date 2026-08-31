package com.solaria.persistence.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ServiceTokenRefreshRequestDTO {


    @NotBlank(message = "refreshToken é obrigatório")
    private String refreshToken;
}
