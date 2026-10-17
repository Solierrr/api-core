package com.solaria.persistence.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import com.solaria.persistence.service.professional.ProfessionalOnboardingService;

class MobileSessionGuardTest {

    private static final String CLIENT = "solaria-mobile";

    private ProfessionalOnboardingService onboarding;
    private MobileSessionGuard guard;

    @BeforeEach
    void setUp() {
        onboarding = mock(ProfessionalOnboardingService.class);
        guard = new MobileSessionGuard(onboarding, "http://localhost:1", "", CLIENT);
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(String clientId, String context) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject(UUID.randomUUID().toString())
                .claim("client_id", clientId).claim("context", context).claim("sid", UUID.randomUUID().toString())
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @Test
    void ignoresRequestsWithoutJwtAndTokensOfOtherClients() {
        assertDoesNotThrow(() -> guard.requireAccess("GET /api/offers"));

        authenticate("web", "company");
        assertDoesNotThrow(() -> guard.requireAccess("GET /api/offers"));
    }

    @Test
    void mobileTokensOnlyReachTheAllowedEndpoints() {
        authenticate(CLIENT, "professional");

        ResponseStatusException denied = assertThrows(ResponseStatusException.class,
                () -> guard.requireAccess("GET /api/offers"));
        assertEquals(HttpStatus.FORBIDDEN, denied.getStatusCode());
    }

    @Test
    void professionalContextOutsideTheMobileClientIsDenied() {
        authenticate("web", "professional");

        assertThrows(ResponseStatusException.class, () -> guard.requireAccess("GET /api/users/me"));
    }

    @Test
    void ineligibleProfessionalsAreDenied() {
        authenticate(CLIENT, "professional");
        when(onboarding.eligibility(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new com.solaria.persistence.dto.professional.ProfessionalOnboardingContract.Eligibility(null, null, null, false, false, false, "UNDER_ANALYSIS", false, java.util.List.of(), 0L, Instant.now()));

        assertThrows(ResponseStatusException.class, () -> guard.requireAccess("GET /api/users/me"));
    }
}
