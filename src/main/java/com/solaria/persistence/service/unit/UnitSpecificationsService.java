package com.solaria.persistence.service.unit;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.solaria.persistence.domain.entity.unit.LocalUnit;
import com.solaria.persistence.domain.entity.unit.UnitSpecifications;
import com.solaria.persistence.dto.request.unit.UnitSpecificationsRequestDTO;
import com.solaria.persistence.dto.response.unit.UnitSpecificationsResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import com.solaria.persistence.repository.unit.LocalUnitRepository;
import com.solaria.persistence.repository.unit.UnitSpecificationsRepository;

@Service
public class UnitSpecificationsService {

    private final UnitSpecificationsRepository unitSpecificationsRepository;
    private final LocalUnitRepository localUnitRepository;
    private final ObjectMapper objectMapper;
    private final RbacAuthorizationService rbac;

    public UnitSpecificationsService(UnitSpecificationsRepository unitSpecificationsRepository,
                                     LocalUnitRepository localUnitRepository,
                                     ObjectMapper objectMapper, RbacAuthorizationService rbac) {
        this.unitSpecificationsRepository = unitSpecificationsRepository;
        this.localUnitRepository = localUnitRepository;
        this.objectMapper = objectMapper;
        this.rbac = rbac;
    }

    @Transactional
    public UnitSpecificationsResponseDTO save(UnitSpecificationsRequestDTO dto) {
        LocalUnit localUnit = localUnitRepository.findForManagementById(dto.getLocalUnitId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade Local não encontrada com ID: " + dto.getLocalUnitId()));
        rbac.requireOwnCompany(localUnit.getRequester().getCompany().getId());

        UnitSpecifications unitSpecifications = new UnitSpecifications();
        unitSpecifications.setLocalUnit(localUnit);
        unitSpecifications.setSpecifications(dto.getSpecifications());
        unitSpecifications.setLocationPhotos(dto.getLocationPhotos());
        unitSpecifications.setDate(OffsetDateTime.now());

        return toResponse(unitSpecificationsRepository.save(unitSpecifications));
    }

    @Transactional(readOnly = true)
    public UnitSpecificationsResponseDTO findById(UUID id) {
        UnitSpecifications unitSpecifications = unitSpecificationsRepository.findForManagementById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Especificação de Unidade não encontrada com ID: " + id));
        rbac.requireOwnCompany(unitSpecifications.getLocalUnit().getRequester().getCompany().getId());
        return toResponse(unitSpecifications);
    }

    @Transactional(readOnly = true)
    public UnitSpecificationsResponseDTO findById(UUID id, UUID companyId) {
        UnitSpecifications unitSpecifications = unitSpecificationsRepository.findForManagementById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado com ID: " + id));
        rbac.requireOwnCompany(unitSpecifications.getLocalUnit().getRequester().getCompany().getId());
        rbac.requireOwnCompany(companyId);
        return toResponse(unitSpecifications);
    }

    @Transactional(readOnly = true)
    public List<UnitSpecificationsResponseDTO> findByLocalUnit(UUID localUnitId) {
        LocalUnit localUnit = localUnitRepository.findForManagementById(localUnitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unidade Local não encontrada com ID: " + localUnitId));
        rbac.requireOwnCompany(localUnit.getRequester().getCompany().getId());
        return unitSpecificationsRepository.findByLocalUnitIdOrderByDateDesc(localUnitId).stream()
                .map(this::toResponse)
                .toList();
    }

    private UnitSpecificationsResponseDTO toResponse(UnitSpecifications unitSpecifications) {
        UnitSpecificationsResponseDTO response = objectMapper.convertValue(unitSpecifications, UnitSpecificationsResponseDTO.class);
        response.setLocalUnitId(unitSpecifications.getLocalUnit().getId());
        return response;
    }
}
