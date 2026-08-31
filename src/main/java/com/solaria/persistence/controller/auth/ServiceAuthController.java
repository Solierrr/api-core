package com.solaria.persistence.controller.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.request.auth.ServiceTokenMintRequestDTO;
import com.solaria.persistence.dto.request.auth.ServiceTokenRefreshRequestDTO;
import com.solaria.persistence.dto.response.auth.ServiceTokenResponseDTO;
import com.solaria.persistence.exception.ServiceAuthException;
import com.solaria.persistence.security.service.ServiceTokenProperties;
import com.solaria.persistence.security.service.ServiceTokenProvider;

import jakarta.validation.Valid;

/**
 * Controller para autentificação M2M(APIs)
 */
@RestController
@RequestMapping("/internal/service-tokens")
public class ServiceAuthController {

    // classe emissora/validadora de token -> usada para mint(emição) e refresh(renovação)
    private final ServiceTokenProvider serviceTokenProvider;

    // classe com as propriedades do token 
    private final ServiceTokenProperties serviceTokenProperties;

    public ServiceAuthController(ServiceTokenProvider serviceTokenProvider,
            ServiceTokenProperties serviceTokenProperties) {
        this.serviceTokenProvider = serviceTokenProvider;
        this.serviceTokenProperties = serviceTokenProperties;
    }


    // Emite um novo par de access+refresh token, fazendo a autentificação com clientSecret
    @PostMapping
    public ResponseEntity<ServiceTokenResponseDTO> mint(@Valid @RequestBody ServiceTokenMintRequestDTO dto) {
        // Lança ServiceAuthException (-> 401) se o secret não bater;
        validateClientSecret(dto.getClientSecret());
        return ResponseEntity.ok(buildResponse(
                serviceTokenProvider.mintAccessToken(),
                serviceTokenProvider.mintRefreshToken()));
    }

    // Troca um refresh token por um acess token, devolvendo o refresh usado e o novo acess token
    @PostMapping("/refresh")
    public ResponseEntity<ServiceTokenResponseDTO> refresh(@Valid @RequestBody ServiceTokenRefreshRequestDTO dto) {
        serviceTokenProvider.parseAndValidate(dto.getRefreshToken(), ServiceTokenProvider.TOKEN_TYPE_REFRESH);
        return ResponseEntity.ok(buildResponse(
                serviceTokenProvider.mintAccessToken(),
                dto.getRefreshToken()));
    }

    private void validateClientSecret(String providedSecret) {
        byte[] provided = (providedSecret == null ? "" : providedSecret).getBytes(StandardCharsets.UTF_8);
        byte[] expected = serviceTokenProperties.getClientSecret().getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(provided, expected)) {
            throw new ServiceAuthException("clientSecret inválido.");
        }
    }

    private ServiceTokenResponseDTO buildResponse(String accessToken, String refreshToken) {
        return ServiceTokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(serviceTokenProperties.getAccessTtl().toSeconds())
                .build();
    }
}
