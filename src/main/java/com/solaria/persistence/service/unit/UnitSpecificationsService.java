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
import com.solaria.persistence.repository.unit.LocalUnitRepository;
import com.solaria.persistence.repository.unit.UnitSpecificationsRepository;

@Service
public class UnitSpecificationsService {

    private final UnitSpecificationsRepository unitSpecificationsRepository;
    private final LocalUnitRepository localUnitRepository;
    private final ObjectMapper objectMapper;

    public UnitSpecificationsService(UnitSpecificationsRepository unitSpecificationsRepository,
                                     LocalUnitRepository localUnitRepository,
                                     ObjectMapper objectMapper) {
        this.unitSpecificationsRepository = unitSpecificationsRepository;
        this.localUnitRepository = localUnitRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UnitSpecificationsResponseDTO save(UnitSpecificationsRequestDTO dto) {
        LocalUnit localUnit = localUnitRepository.findById(dto.getLocalUnitId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade Local não encontrada com ID: " + dto.getLocalUnitId()));

        UnitSpecifications unitSpecifications = new UnitSpecifications();
        unitSpecifications.setLocalUnit(localUnit);
        unitSpecifications.setSpecifications(dto.getSpecifications());
        unitSpecifications.setLocationPhotos(dto.getLocationPhotos());
        unitSpecifications.setDate(OffsetDateTime.now());

        return toResponse(unitSpecificationsRepository.save(unitSpecifications));
    }

    @Transactional(readOnly = true)
    public UnitSpecificationsResponseDTO findById(UUID id) {
        UnitSpecifications unitSpecifications = unitSpecificationsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Especificação de Unidade não encontrada com ID: " + id));
        return toResponse(unitSpecifications);
    }

    @Transactional(readOnly = true)
    public UnitSpecificationsResponseDTO findById(UUID id, UUID companyId) {
        UnitSpecifications unitSpecifications = unitSpecificationsRepository
                .findByIdAndLocalUnit_Requester_Company_Id(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Especificação de Unidade não encontrada com ID: " + id));
        return toResponse(unitSpecifications);
    }

    @Transactional(readOnly = true)
    public List<UnitSpecificationsResponseDTO> findByLocalUnit(UUID localUnitId) {
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
