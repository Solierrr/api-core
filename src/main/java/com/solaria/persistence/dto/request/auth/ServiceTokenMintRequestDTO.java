package com.solaria.persistence.dto.request.auth;


import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceTokenMintRequestDTO {

    @NotBlank(message = "clientSecret é obrigatório")
    private String clientSecret;
}
