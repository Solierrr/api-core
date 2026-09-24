package com.solaria.persistence.service.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;
import com.solaria.persistence.dto.request.identity.PermissionRequestDTO;
import com.solaria.persistence.dto.response.identity.PermissionResponseDTO;
import com.solaria.persistence.domain.entity.identity.Permission;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.identity.PermissionRepository;
import com.solaria.persistence.repository.identity.PositionPermissionRepository;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PositionPermissionRepository positionPermissionRepository;
    private final ObjectMapper objectMapper;

    public PermissionService(PermissionRepository permissionRepository,
                              PositionPermissionRepository positionPermissionRepository,
                              ObjectMapper objectMapper) {
        this.permissionRepository = permissionRepository;
        this.positionPermissionRepository = positionPermissionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PermissionResponseDTO save(PermissionRequestDTO dto) {
        if (permissionRepository.existsByPermissionName(dto.getPermissionName())) {
            throw new DuplicateResourceException("Permissão já cadastrada com o nome: " + dto.getPermissionName());
        }

        Permission permission = new Permission();
        permission.setPermissionName(dto.getPermissionName());
        permission.setName(dto.getName());
        permission.setDescription(dto.getDescription());

        return toResponse(permissionRepository.save(permission));
    }

    @Transactional
    public PermissionResponseDTO update(UUID id, PermissionRequestDTO dto) {
        Permission permission = permissionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Permissão com id:" + id + " não encontrada para atualização"));

        if (permissionRepository.existsByPermissionNameAndIdNot(dto.getPermissionName(), id)) {
            throw new DuplicateResourceException("Permissão já cadastrada com o nome: " + dto.getPermissionName());
        }

        permission.setPermissionName(dto.getPermissionName());
        permission.setName(dto.getName());
        permission.setDescription(dto.getDescription());

        return toResponse(permissionRepository.save(permission));
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!permissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Permissão com id:" + id + " não encontrada para exclusão");
        }
        positionPermissionRepository.deleteByPermissionId(id);
        permissionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PermissionResponseDTO findById(UUID id) {
        Permission permission = permissionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Permissão não encontrada com ID: " + id));
        return toResponse(permission);
    }

    @Transactional(readOnly = true)
    public List<PermissionResponseDTO> findAll() {
        return permissionRepository.findAll().stream().map(this::toResponse).toList();
    }

    private PermissionResponseDTO toResponse(Permission entity) {
        return objectMapper.convertValue(entity, PermissionResponseDTO.class);
    }
}
