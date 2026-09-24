package com.solaria.persistence.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.solaria.persistence.domain.entity.User;
import com.solaria.persistence.dto.request.ConnectionRequestDTO;
import com.solaria.persistence.dto.response.UserResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.UserRepository;
import com.solaria.persistence.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class ConnectionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    private ConnectionService connectionService;
    private User owner;
    private User connectedUser;

    @BeforeEach
    void setUp() {
        connectionService = new ConnectionService(userRepository, currentUserService);
        owner = user(UUID.randomUUID(), "owner", true);
        connectedUser = user(UUID.randomUUID(), "connected", true);
    }

    @Test
    void shouldAddConnectedUserIdToCurrentUserArray() {
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(userRepository.findByIdForUpdate(owner.getId())).thenReturn(Optional.of(owner));
        when(userRepository.findById(connectedUser.getId())).thenReturn(Optional.of(connectedUser));

        UserResponseDTO response = connectionService.save(requestFor(connectedUser.getId()));

        assertEquals(List.of(connectedUser.getId()), owner.getConnections());
        assertEquals(connectedUser.getId(), response.getId());
        assertEquals(connectedUser.getAuth_id(), response.getAuthId());
        verify(userRepository).save(owner);
    }

    @Test
    void shouldRejectSelfConnection() {
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(userRepository.findByIdForUpdate(owner.getId())).thenReturn(Optional.of(owner));

        assertThrows(InvalidFieldException.class,
                () -> connectionService.save(requestFor(owner.getId())));

        verify(userRepository, never()).save(owner);
    }

    @Test
    void shouldRejectInactiveConnectedUser() {
        connectedUser.setActive(false);
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(userRepository.findByIdForUpdate(owner.getId())).thenReturn(Optional.of(owner));
        when(userRepository.findById(connectedUser.getId())).thenReturn(Optional.of(connectedUser));

        assertThrows(BusinessRuleException.class,
                () -> connectionService.save(requestFor(connectedUser.getId())));

        verify(userRepository, never()).save(owner);
    }

    @Test
    void shouldRejectDuplicateConnection() {
        owner.setConnections(new ArrayList<>(List.of(connectedUser.getId())));
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(userRepository.findByIdForUpdate(owner.getId())).thenReturn(Optional.of(owner));
        when(userRepository.findById(connectedUser.getId())).thenReturn(Optional.of(connectedUser));

        assertThrows(DuplicateResourceException.class,
                () -> connectionService.save(requestFor(connectedUser.getId())));

        verify(userRepository, never()).save(owner);
    }

    @Test
    void shouldReturnNotFoundWhenConnectedUserDoesNotExist() {
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(userRepository.findByIdForUpdate(owner.getId())).thenReturn(Optional.of(owner));
        when(userRepository.findById(connectedUser.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> connectionService.save(requestFor(connectedUser.getId())));
    }

    @Test
    void shouldListConnectedUsersInArrayOrder() {
        User secondConnectedUser = user(UUID.randomUUID(), "second", true);
        owner.setConnections(List.of(connectedUser.getId(), secondConnectedUser.getId()));
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(userRepository.findAllById(owner.getConnections()))
                .thenReturn(List.of(secondConnectedUser, connectedUser));

        List<UserResponseDTO> response = connectionService.findAll();

        assertEquals(2, response.size());
        assertEquals(connectedUser.getId(), response.get(0).getId());
        assertEquals(secondConnectedUser.getId(), response.get(1).getId());
    }

    @Test
    void shouldReturnEmptyConnectionList() {
        when(currentUserService.getCurrentUser()).thenReturn(owner);

        assertTrue(connectionService.findAll().isEmpty());
        verify(userRepository, never()).findAllById(owner.getConnections());
    }

    @Test
    void shouldReturnConnectedUserById() {
        owner.setConnections(List.of(connectedUser.getId()));
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(userRepository.findById(connectedUser.getId())).thenReturn(Optional.of(connectedUser));

        UserResponseDTO response = connectionService.findById(connectedUser.getId());

        assertEquals(connectedUser.getId(), response.getId());
    }

    @Test
    void shouldHideUserOutsideCurrentUserConnections() {
        when(currentUserService.getCurrentUser()).thenReturn(owner);

        assertThrows(ResourceNotFoundException.class,
                () -> connectionService.findById(connectedUser.getId()));

        verify(userRepository, never()).findById(connectedUser.getId());
    }

    @Test
    void shouldRemoveOnlyRequestedConnection() {
        UUID preservedConnectionId = UUID.randomUUID();
        owner.setConnections(new ArrayList<>(List.of(
                connectedUser.getId(), preservedConnectionId)));
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(userRepository.findByIdForUpdate(owner.getId())).thenReturn(Optional.of(owner));

        connectionService.deleteById(connectedUser.getId());

        assertEquals(List.of(preservedConnectionId), owner.getConnections());
        verify(userRepository).save(owner);
    }

    @Test
    void shouldReturnNotFoundWhenRemovingUnknownConnection() {
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(userRepository.findByIdForUpdate(owner.getId())).thenReturn(Optional.of(owner));

        assertThrows(ResourceNotFoundException.class,
                () -> connectionService.deleteById(connectedUser.getId()));

        verify(userRepository, never()).save(owner);
    }

    private ConnectionRequestDTO requestFor(UUID connectedUserId) {
        ConnectionRequestDTO request = new ConnectionRequestDTO();
        request.setConnectedUserId(connectedUserId);
        return request;
    }

    private User user(UUID id, String username, boolean active) {
        User user = new User();
        user.setId(id);
        user.setAuth_id(UUID.randomUUID());
        user.setUsername(username);
        user.setAvatar("https://example.com/" + username + ".png");
        user.setBanner("https://example.com/" + username + "-banner.png");
        user.setActive(active);
        user.setConnections(new ArrayList<>());
        return user;
    }
}
