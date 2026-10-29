package com.solaria.persistence.service.execution;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.solaria.persistence.domain.entity.execution.TechnicalProject;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.execution.TechnicalProjectRepository;
import com.solaria.persistence.repository.identity.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProjectRequesterAccessServiceTest {

    @Mock
    private TechnicalProjectRepository technicalProjectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectAccess projectAccess;
    @InjectMocks
    private ProjectRequesterAccessService service;

    private final UUID projectId = UUID.randomUUID();
    private final UUID authId = UUID.randomUUID();
    private final TechnicalProject project = new TechnicalProject();
    private final User user = new User();

    @BeforeEach
    void setUp() {
        user.setActive(true);
    }

    @Test
    void shouldAllowTheRequester() {
        when(technicalProjectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findByAuthId(authId)).thenReturn(Optional.of(user));
        when(projectAccess.isOwner(project, user)).thenReturn(true);

        assertDoesNotThrow(() -> service.requireRequester(projectId, authId));
    }

    @Test
    void shouldRejectAnotherUser() {
        when(technicalProjectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findByAuthId(authId)).thenReturn(Optional.of(user));
        when(projectAccess.isOwner(project, user)).thenReturn(false);

        assertThrows(UnauthorizedAccessException.class, () -> service.requireRequester(projectId, authId));
    }

    @Test
    void shouldRejectAnInactiveRequester() {
        user.setActive(false);
        when(technicalProjectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findByAuthId(authId)).thenReturn(Optional.of(user));

        assertThrows(UnauthorizedAccessException.class, () -> service.requireRequester(projectId, authId));
    }

    @Test
    void shouldFailWhenTheUserDoesNotExist() {
        when(technicalProjectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findByAuthId(authId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.requireRequester(projectId, authId));
    }

    @Test
    void shouldFailWhenTheProjectDoesNotExist() {
        when(technicalProjectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.requireRequester(projectId, authId));
    }
}
