package com.solaria.persistence.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.identity.UserRepository;

@Component
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            return Optional.empty();
        }

        UUID authId;
        try {
            authId = UUID.fromString(jwtAuthentication.getToken().getSubject());
        } catch (IllegalArgumentException exception) {
            throw new UnauthorizedAccessException("O subject do token de acesso é inválido");
        }
        return userRepository.findByAuthId(authId);
    }

    public User getCurrentUser() {
        return findCurrentUser().orElseThrow(
                () -> new ResourceNotFoundException("Usuário autenticado não encontrado na api-core"));
    }
}
