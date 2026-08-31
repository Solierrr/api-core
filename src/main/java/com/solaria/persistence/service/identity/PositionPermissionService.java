package com.solaria.persistence.service.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.dto.response.identity.PermissionResponseDTO;
import com.solaria.persistence.dto.request.identity.PositionPermissionRequestDTO;
import com.solaria.persistence.dto.response.identity.PositionPermissionResponseDTO;
import com.solaria.persistence.domain.entity.identity.Permission;
import com.solaria.persistence.domain.entity.identity.Position;
import com.solaria.persistence.domain.entity.identity.PositionPermission;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.identity.PermissionRepository;
import com.solaria.persistence.repository.identity.PositionPermissionRepository;
import com.solaria.persistence.repository.identity.PositionRepository;


@Service
public class PositionPermissionService {

    private final PositionPermissionRepository positionPermissionRepository;
    private final PositionRepository positionRepository;
    private final PermissionRepository permissionRepository;

    public PositionPermissionService(PositionPermissionRepository positionPermissionRepository,
                                     PositionRepository positionRepository,
                                     PermissionRepository permissionRepository) {
        this.positionPermissionRepository = positionPermissionRepository;
        this.positionRepository = positionRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    public PositionPermissionResponseDTO grant(PositionPermissionRequestDTO dto) {
        Position position = positionRepository.findById(dto.getPositionId()).orElseThrow(
                () -> new ResourceNotFoundException("Cargo não encontrado com ID: " + dto.getPositionId()));
        Permission permission = permissionRepository.findById(dto.getPermissionId()).orElseThrow(
                () -> new ResourceNotFoundException("Permissão não encontrada com ID: " + dto.getPermissionId()));

        if (positionPermissionRepository.existsByPositionIdAndPermissionId(dto.getPositionId(), dto.getPermissionId())) {
            throw new DuplicateResourceException(
                    "Permissão já concedida ao cargo: " + dto.getPositionId());
        }

        PositionPermission positionPermission = new PositionPermission();
        positionPermission.setPosition(position);
        positionPermission.setPermission(permission);

        return toResponse(positionPermissionRepository.save(positionPermission));
    }

    @Transactional
    public void revoke(UUID id) {
        PositionPermission positionPermission = positionPermissionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Vínculo cargo-permissão com id:" + id + " não encontrado(a) para exclusão"));

        if (Position.ADMIN_NAME.equals(positionPermission.getPosition().getName())) {
            throw new UnauthorizedAccessException("Revogar permissão de ADMIN não é permitido");
        }

        positionPermissionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<PositionPermissionResponseDTO> findByPosition(UUID positionId) {
        return positionPermissionRepository.findByPositionId(positionId).stream().map(this::toResponse).toList();
    }

    private PositionPermissionResponseDTO toResponse(PositionPermission entity) {
        PositionPermissionResponseDTO response = new PositionPermissionResponseDTO();
        response.setId(entity.getId());
        response.setPositionId(entity.getPosition().getId());
        response.setPermission(toPermissionResponse(entity.getPermission()));
        return response;
    }

    private PermissionResponseDTO toPermissionResponse(Permission permission) {
        if (permission == null) {
            return null;
        }
        PermissionResponseDTO dto = new PermissionResponseDTO();
        dto.setId(permission.getId());
        dto.setPermissionName(permission.getPermissionName());
        return dto;
    }
}
