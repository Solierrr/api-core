package com.solaria.persistence.dto.auth;

import lombok.Getter;
import lombok.Setter;

// Espelha o AuthResponse de api-auth (POST /auth/login)
@Setter
@Getter
public class AuthLoginResponseDTO {

    private String accessToken;

    private String refreshToken;

    private String accessTokenExpiresAt;

    private String userId;

    private String email;

}
