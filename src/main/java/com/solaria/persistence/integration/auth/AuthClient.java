package com.solaria.persistence.integration.auth;

import com.solaria.persistence.dto.auth.AuthLoginResponseDTO;
import com.solaria.persistence.dto.auth.LoginRequestDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

// Cliente do endpoint POST /auth/login em api-auth
@Component
public class AuthClient {

    private final RestClient restClient;

    public AuthClient(RestClient authRestClient) {
        this.restClient = authRestClient;
    }

    public AuthLoginResponseDTO login(String email, String password) {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(email);
        request.setPassword(password);
        try {
            return restClient.post()
                    .uri("/auth/login")
                    .body(request)
                    .retrieve()
                    .body(AuthLoginResponseDTO.class);
        } catch (RestClientResponseException e) {
            throw new AuthIntegrationException(
                    "Falha ao autenticar em api-auth: HTTP " + e.getStatusCode().value(), e);
        }
    }
}
