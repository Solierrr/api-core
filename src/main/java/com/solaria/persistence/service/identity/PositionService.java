package com.solaria.persistence.service.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.dto.request.identity.PositionRequestDTO;
import com.solaria.persistence.dto.response.identity.PositionResponseDTO;
import com.solaria.persistence.domain.entity.identity.Position;
import com.solaria.persistence.domain.entity.company.CompanyPositions;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.company.CompanyPositionsRepository;
import com.solaria.persistence.repository.identity.PositionPermissionRepository;
import com.solaria.persistence.repository.identity.PositionRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;


@Service
public class PositionService {

    private final PositionRepository positionRepository;
    private final PositionPermissionRepository positionPermissionRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final CompanyPositionsRepository companyPositionsRepository;
    private final CompanyRepository companyRepository;
    private final RbacAuthorizationService rbac;

    public PositionService(PositionRepository positionRepository,
                           PositionPermissionRepository positionPermissionRepository,
                           UserCompanyRepository userCompanyRepository,
                           CompanyPositionsRepository companyPositionsRepository,
                           CompanyRepository companyRepository,
                           RbacAuthorizationService rbac) {
        this.positionRepository = positionRepository;
        this.positionPermissionRepository = positionPermissionRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.companyPositionsRepository = companyPositionsRepository;
        this.companyRepository = companyRepository;
        this.rbac = rbac;
    }

    @Transactional
    public PositionResponseDTO save(PositionRequestDTO dto) {
        Position position = new Position();
        position.setName(commonName(dto.getName()));
        UUID companyId = rbac.getCurrentCompanyId();
        positionRepository.save(position);
        CompanyPositions association = new CompanyPositions();
        association.setCompany(companyRepository.getReferenceById(companyId));
        association.setPosition(position);
        companyPositionsRepository.save(association);

        return toResponse(position, companyId);
    }

    @Transactional
    public PositionResponseDTO update(UUID id, PositionRequestDTO dto) {
        CompanyPositions association = ownedPosition(id);
        Position position = association.getPosition();

        if (isAdmin(position.getName())) {
            throw new InvalidFieldException("Cargo ADMIN não pode ser renomeado");
        }

        position.setName(commonName(dto.getName()));

        return toResponse(positionRepository.save(position), association.getCompany().getId());
    }

    @Transactional
    public void deleteById(UUID id) {
        CompanyPositions association = ownedPosition(id);
        Position position = association.getPosition();

        if (Position.ADMIN_NAME.equals(position.getName())) {
            throw new UnauthorizedAccessException("Cargo ADMIN não pode ser excluído");
        }

        if (userCompanyRepository.existsByPositionId(id)) {
            throw new ResourceInUseException("Cargo não pode ser excluído(a): possui usuário(s) vinculado(s)");
        }

        positionPermissionRepository.deleteByPositionId(id);
        companyPositionsRepository.delete(association);
        positionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PositionResponseDTO findById(UUID id) {
        CompanyPositions association = ownedPosition(id);
        return toResponse(association.getPosition(), association.getCompany().getId());
    }

    @Transactional(readOnly = true)
    public List<PositionResponseDTO> findAll() {
        UUID companyId = rbac.getCurrentCompanyId();
        return companyPositionsRepository.findByCompanyId(companyId).stream()
                .map(association -> toResponse(association.getPosition(), companyId)).toList();
    }

    private CompanyPositions ownedPosition(UUID id) {
        CompanyPositions association = companyPositionsRepository.findByPositionId(id).orElseThrow(
                () -> new ResourceNotFoundException("Cargo não encontrado com ID: " + id));
        rbac.requireOwnCompany(association.getCompany().getId());
        return association;
    }

    private String commonName(String name) {
        if (name == null || name.isBlank() || name.trim().length() > 12 || isAdmin(name)) {
            throw new InvalidFieldException("Nome de cargo inválido ou reservado");
        }
        return name.trim();
    }

    private boolean isAdmin(String name) {
        return name != null && Position.ADMIN_NAME.equalsIgnoreCase(name.trim());
    }

    private PositionResponseDTO toResponse(Position position, UUID companyId) {
        PositionResponseDTO response = new PositionResponseDTO();
        response.setId(position.getId());
        response.setName(position.getName());
        response.setCompanyId(companyId);
        return response;
    }
}
