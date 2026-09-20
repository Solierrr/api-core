package com.solaria.persistence.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.ConnectionRequestDTO;
import com.solaria.persistence.dto.response.UserResponseDTO;
import com.solaria.persistence.service.ConnectionService;

@ExtendWith(MockitoExtension.class)
class ConnectionControllerTest {

    @Mock
    private ConnectionService connectionService;

    private ConnectionController controller;

    @BeforeEach
    void setUp() {
        controller = new ConnectionController(connectionService);
    }

    @Test
    void shouldReturnCreatedUserAndLocation() {
        ConnectionRequestDTO request = new ConnectionRequestDTO();
        request.setConnectedUserId(UUID.randomUUID());
        UserResponseDTO response = new UserResponseDTO();
        response.setId(request.getConnectedUserId());
        when(connectionService.save(request)).thenReturn(response);

        ResponseEntity<UserResponseDTO> result = controller.save(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
        assertEquals("/api/connections/" + response.getId(),
                result.getHeaders().getLocation().toString());
    }

    @Test
    void shouldReturnConnectedUsers() {
        UserResponseDTO connectedUser = new UserResponseDTO();
        when(connectionService.findAll()).thenReturn(List.of(connectedUser));

        ResponseEntity<List<UserResponseDTO>> result = controller.findAll();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(List.of(connectedUser), result.getBody());
    }

    @Test
    void shouldReturnConnectedUserById() {
        UUID id = UUID.randomUUID();
        UserResponseDTO connectedUser = new UserResponseDTO();
        connectedUser.setId(id);
        when(connectionService.findById(id)).thenReturn(connectedUser);

        ResponseEntity<UserResponseDTO> result = controller.findById(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(connectedUser, result.getBody());
    }

    @Test
    void shouldReturnNoContentAfterDelete() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> result = controller.deleteById(id);

        verify(connectionService).deleteById(id);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
    }
}
