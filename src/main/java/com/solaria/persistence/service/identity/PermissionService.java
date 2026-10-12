package com.solaria.persistence.service.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.solaria.persistence.domain.entity.identity.Permission;
import com.solaria.persistence.dto.response.identity.PermissionResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.identity.PermissionRepository;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final ObjectMapper objectMapper;

    public PermissionResponseDTO findById(UUID id) {
        Permission permission = permissionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Permissão não encontrado(a) com ID: " + id));
        return toResponse(permission);
    }

    public List<PermissionResponseDTO> findAll() {
        return permissionRepository.findAll().stream().map(this::toResponse).toList();
    }

    private PermissionResponseDTO toResponse(Permission permission) {
        return objectMapper.convertValue(permission, PermissionResponseDTO.class);
    }
}
