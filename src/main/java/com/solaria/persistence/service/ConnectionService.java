package com.solaria.persistence.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.User;
import com.solaria.persistence.dto.request.ConnectionRequestDTO;
import com.solaria.persistence.dto.response.UserResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.UserRepository;
import com.solaria.persistence.security.CurrentUserService;

@Service
public class ConnectionService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public ConnectionService(UserRepository userRepository,
                             CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public UserResponseDTO save(ConnectionRequestDTO dto) {
        User currentUser = currentUserService.getCurrentUser();
        User owner = findOwnerForUpdate(currentUser.getId());

        if (owner.getId().equals(dto.getConnectedUserId())) {
            throw new InvalidFieldException("Um usuário não pode conectar-se consigo mesmo");
        }

        User connectedUser = userRepository.findById(dto.getConnectedUserId()).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Usuário não encontrado com ID: " + dto.getConnectedUserId()));

        if (!Boolean.TRUE.equals(connectedUser.getActive())) {
            throw new BusinessRuleException("Não é possível conectar-se a um usuário inativo");
        }

        List<UUID> connections = mutableConnections(owner);
        if (connections.contains(connectedUser.getId())) {
            throw new DuplicateResourceException("A conexão entre os usuários já existe");
        }

        connections.add(connectedUser.getId());
        owner.setConnections(connections);
        userRepository.save(owner);
        return toUserResponse(connectedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        List<UUID> connectionIds = connectionsOf(currentUserService.getCurrentUser());
        if (connectionIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, User> usersById = new HashMap<>();
        userRepository.findAllById(connectionIds)
                .forEach(user -> usersById.put(user.getId(), user));

        return connectionIds.stream()
                .map(usersById::get)
                .filter(user -> user != null)
                .map(this::toUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID connectedUserId) {
        User owner = currentUserService.getCurrentUser();
        requireConnection(owner, connectedUserId);
        User connectedUser = userRepository.findById(connectedUserId).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Usuário conectado não encontrado com ID: " + connectedUserId));
        return toUserResponse(connectedUser);
    }

    @Transactional
    public void deleteById(UUID connectedUserId) {
        User currentUser = currentUserService.getCurrentUser();
        User owner = findOwnerForUpdate(currentUser.getId());
        List<UUID> connections = mutableConnections(owner);
        if (!connections.remove(connectedUserId)) {
            throw new ResourceNotFoundException(
                    "Conexão não encontrada com o usuário de ID: " + connectedUserId);
        }
        owner.setConnections(connections);
        userRepository.save(owner);
    }

    private User findOwnerForUpdate(UUID ownerId) {
        return userRepository.findByIdForUpdate(ownerId).orElseThrow(
                () -> new ResourceNotFoundException("Usuário autenticado não encontrado na api-core"));
    }

    private void requireConnection(User owner, UUID connectedUserId) {
        if (!connectionsOf(owner).contains(connectedUserId)) {
            throw new ResourceNotFoundException(
                    "Conexão não encontrada com o usuário de ID: " + connectedUserId);
        }
    }

    private List<UUID> connectionsOf(User user) {
        return user.getConnections() == null ? List.of() : user.getConnections();
    }

    private List<UUID> mutableConnections(User user) {
        return new ArrayList<>(connectionsOf(user));
    }

    private UserResponseDTO toUserResponse(User user) {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(user.getId());
        response.setAuthId(user.getAuth_id());
        response.setUsername(user.getUsername());
        response.setAvatar(user.getAvatar());
        response.setBanner(user.getBanner());
        response.setActive(user.getActive());
        return response;
    }
}
