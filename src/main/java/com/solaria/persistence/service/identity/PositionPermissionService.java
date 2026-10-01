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
import com.solaria.persistence.repository.company.CompanyPositionsRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;


@Service
public class PositionPermissionService {

    private final PositionPermissionRepository positionPermissionRepository;
    private final PositionRepository positionRepository;
    private final PermissionRepository permissionRepository;
    private final CompanyPositionsRepository companyPositionsRepository;
    private final RbacAuthorizationService rbac;

    public PositionPermissionService(PositionPermissionRepository positionPermissionRepository,
                                     PositionRepository positionRepository,
                                     PermissionRepository permissionRepository,
                                     CompanyPositionsRepository companyPositionsRepository,
                                     RbacAuthorizationService rbac) {
        this.positionPermissionRepository = positionPermissionRepository;
        this.positionRepository = positionRepository;
        this.permissionRepository = permissionRepository;
        this.companyPositionsRepository = companyPositionsRepository;
        this.rbac = rbac;
    }

    @Transactional
    public PositionPermissionResponseDTO grant(PositionPermissionRequestDTO dto) {
        Position position = positionRepository.findById(dto.getPositionId()).orElseThrow(
                () -> new ResourceNotFoundException("Cargo não encontrado com ID: " + dto.getPositionId()));
        Permission permission = permissionRepository.findById(dto.getPermissionId()).orElseThrow(
                () -> new ResourceNotFoundException("Permissão não encontrada com ID: " + dto.getPermissionId()));

        requireMutablePosition(position);
        if (!rbac.hasEndpointAccess(permission.getPermissionName())) {
            throw new UnauthorizedAccessException("Não é permitido conceder uma permissão que o próprio acesso não possui");
        }

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

        requireMutablePosition(positionPermission.getPosition());

        positionPermissionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<PositionPermissionResponseDTO> findByPosition(UUID positionId) {
        var links = companyPositionsRepository.findByPositionId(positionId);
        if (links.isEmpty()) {
            throw new UnauthorizedAccessException("Cargo não disponível para a empresa");
        }
        UUID companyId = rbac.getCurrentCompanyId();
        boolean ownCompany = links.stream().anyMatch(link -> link.getCompany().getId().equals(companyId));
        if (!ownCompany) {
            throw new UnauthorizedAccessException("Cargo não disponível para a empresa");
        }
        return positionPermissionRepository.findByPositionId(positionId).stream().map(this::toResponse).toList();
    }

    private void requireMutablePosition(Position position) {
        var links = companyPositionsRepository.findByPositionId(position.getId());
        if (Position.ADMIN_NAME.equals(position.getName()) || links.size() != 1) {
            throw new UnauthorizedAccessException("Permissões de cargos compartilhados não podem ser alteradas");
        }
        rbac.requireOwnCompany(links.getFirst().getCompany().getId());
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
        dto.setName(permission.getName());
        dto.setDescription(permission.getDescription());
        return dto;
    }
}
