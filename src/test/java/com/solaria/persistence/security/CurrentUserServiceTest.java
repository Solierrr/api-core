package com.solaria.persistence.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.exception.UserNotProvisionedException;
import com.solaria.persistence.repository.identity.UserRepository;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void bindRequest() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldResolveCurrentUserFromJwtSubject() {
        UUID authId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setAuthId(authId);
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

    @Test
    void shouldReturnProvisionedUserWithDistinctCoreAndAuthIds() {
        UUID authId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setAuthId(authId);
        authenticate(authId.toString());
        when(userRepository.findByAuthId(authId)).thenReturn(Optional.of(user));

        User result = new CurrentUserService(userRepository).getProvisionedCurrentUser();

        assertEquals(user.getId(), result.getId());
        assertEquals(authId, result.getAuthId());
    }

    @Test
    void shouldReturnDedicatedNotProvisionedErrorOnlyForNewHelper() {
        UUID authId = UUID.randomUUID();
        authenticate(authId.toString());
        when(userRepository.findByAuthId(authId)).thenReturn(Optional.empty());
        CurrentUserService service = new CurrentUserService(userRepository);

        UserNotProvisionedException exception = assertThrows(UserNotProvisionedException.class,
                service::getProvisionedCurrentUser);

        assertEquals(404, exception.getStatus().value());
        assertEquals("USER_NOT_PROVISIONED", exception.getErrorCode());
        ResourceNotFoundException legacyException = assertThrows(ResourceNotFoundException.class,
                service::getCurrentUser);
        assertEquals("NOT_FOUND", legacyException.getErrorCode());
        verify(userRepository).findByAuthId(authId);
    }

    @Test
    void shouldReuseLoadedUserForRepeatedIdentityHelpers() {
        UUID authId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        authenticate(authId.toString());
        when(userRepository.findByAuthId(authId)).thenReturn(Optional.of(user));
        CurrentUserService service = new CurrentUserService(userRepository);

        assertEquals(user, service.getCurrentUser());
        assertEquals(user, service.getProvisionedCurrentUser());
        assertEquals(Optional.of(user.getId()), service.findCurrentUserId());
        verify(userRepository).findByAuthId(authId);
        verify(userRepository, never()).findIdByAuthId(any());
    }

    @Test
    void shouldLoadOnlyScalarIdentityUntilUserIsRequired() {
        UUID authId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        authenticate(authId.toString());
        when(userRepository.findIdByAuthId(authId)).thenReturn(Optional.of(userId));
        CurrentUserService service = new CurrentUserService(userRepository);

        assertEquals(Optional.of(userId), service.findCurrentUserId());
        assertEquals(Optional.of(userId), service.findCurrentUserId());
        verify(userRepository).findIdByAuthId(authId);
        verify(userRepository, never()).findByAuthId(any());
        verify(userRepository, never()).findById(any());

        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        assertEquals(user, service.getProvisionedCurrentUser());
        assertEquals(user, service.getCurrentUser());
        verify(userRepository).findById(userId);
    }

    @Test
    void shouldNotReuseRequestFactsOnAnotherThread() throws Exception {
        UUID authId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        authenticate(authId.toString());
        when(userRepository.findIdByAuthId(authId)).thenReturn(Optional.of(userId));
        CurrentUserService service = new CurrentUserService(userRepository);
        assertEquals(Optional.of(userId), service.findCurrentUserId());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        FutureTask<Optional<UUID>> task = new FutureTask<>(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            authenticate(authId.toString());
            try {
                return service.findCurrentUserId();
            } finally {
                SecurityContextHolder.clearContext();
                RequestContextHolder.resetRequestAttributes();
            }
        });
        Thread thread = new Thread(task);
        thread.start();

        assertEquals(Optional.of(userId), task.get(5, TimeUnit.SECONDS));
        verify(userRepository, times(2)).findIdByAuthId(authId);
    }

    @Test
    void provisionedScalarIdentityReusesMemoAndDoesNotMaterializeUser() {
        UUID authId = UUID.randomUUID(); UUID userId = UUID.randomUUID();
        authenticate(authId.toString());
        when(userRepository.findIdByAuthId(authId)).thenReturn(Optional.of(userId));
        CurrentUserService service = new CurrentUserService(userRepository);
        assertEquals(userId, service.getProvisionedCurrentUserId());
        assertEquals(userId, service.getProvisionedCurrentUserId());
        verify(userRepository).findIdByAuthId(authId);
        verify(userRepository, never()).findByAuthId(any());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void missingProvisionedScalarIdentityHasDedicated404Code() {
        UUID authId = UUID.randomUUID(); authenticate(authId.toString());
        when(userRepository.findIdByAuthId(authId)).thenReturn(Optional.empty());
        UserNotProvisionedException error = assertThrows(UserNotProvisionedException.class,
                new CurrentUserService(userRepository)::getProvisionedCurrentUserId);
        assertEquals(404, error.getStatus().value());
        assertEquals("USER_NOT_PROVISIONED", error.getErrorCode());
        verify(userRepository, never()).findByAuthId(any());
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
