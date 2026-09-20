package com.solaria.persistence.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.solaria.persistence.domain.entity.User;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldResolveCurrentUserFromJwtSubject() {
        UUID authId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setAuth_id(authId);
        authenticate(authId.toString());
        when(userRepository.findByAuthId(authId)).thenReturn(Optional.of(user));

        CurrentUserService service = new CurrentUserService(userRepository);

        assertEquals(user, service.getCurrentUser());
    }

    @Test
    void shouldRejectInvalidJwtSubject() {
        authenticate("invalid-subject");
        CurrentUserService service = new CurrentUserService(userRepository);

        assertThrows(UnauthorizedAccessException.class, service::getCurrentUser);
    }

    @Test
    void shouldReturnNotFoundWhenJwtUserIsNotProvisioned() {
        UUID authId = UUID.randomUUID();
        authenticate(authId.toString());
        when(userRepository.findByAuthId(authId)).thenReturn(Optional.empty());
        CurrentUserService service = new CurrentUserService(userRepository);

        assertThrows(ResourceNotFoundException.class, service::getCurrentUser);
    }

    private void authenticate(String subject) {
        Jwt jwt = new Jwt(
                "token",
                null,
                null,
                Map.of("alg", "RS256"),
                Map.of("sub", subject));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }
}
