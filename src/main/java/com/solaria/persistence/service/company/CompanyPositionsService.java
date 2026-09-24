package com.solaria.persistence.service.company;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;
import com.solaria.persistence.dto.request.company.CompanyPositionsRequestDTO;
import com.solaria.persistence.dto.response.company.CompanyPositionsResponseDTO;
import com.solaria.persistence.dto.response.identity.PositionResponseDTO;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.company.CompanyPositions;
import com.solaria.persistence.domain.entity.identity.Position;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.company.CompanyPositionsRepository;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.identity.PositionRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

@Service
public class CompanyPositionsService {

    private final CompanyPositionsRepository companyPositionsRepository;
    private final CompanyRepository companyRepository;
    private final PositionRepository positionRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final RbacAuthorizationService rbac;
    private final ObjectMapper objectMapper;

    public CompanyPositionsService(CompanyPositionsRepository companyPositionsRepository,
                                   CompanyRepository companyRepository,
                                   PositionRepository positionRepository,
                                   UserCompanyRepository userCompanyRepository,
                                   RbacAuthorizationService rbac,
                                   ObjectMapper objectMapper) {
        this.companyPositionsRepository = companyPositionsRepository;
        this.companyRepository = companyRepository;
        this.positionRepository = positionRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.rbac = rbac;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CompanyPositionsResponseDTO save(CompanyPositionsRequestDTO dto) {
        Company company = companyRepository.findById(dto.getCompanyId()).orElseThrow(
                () -> new ResourceNotFoundException("Empresa não encontrada com ID: " + dto.getCompanyId()));
        Position position = positionRepository.findById(dto.getPositionId()).orElseThrow(
                () -> new ResourceNotFoundException("Cargo não encontrado com ID: " + dto.getPositionId()));

        if (companyPositionsRepository.existsByCompanyIdAndPositionId(dto.getCompanyId(), dto.getPositionId())) {
            throw new DuplicateResourceException(
                    "Cargo já disponibilizado para a empresa: " + dto.getCompanyId());
        }

        // primeira company positon não depende de vínculo, pois ele nao existe ainda
        if (companyPositionsRepository.existsByCompanyId(dto.getCompanyId())) {
            rbac.requireOwnCompany(dto.getCompanyId());
        }

        CompanyPositions companyPositions = new CompanyPositions();
        companyPositions.setCompany(company);
        companyPositions.setPosition(position);

        return toResponse(companyPositionsRepository.save(companyPositions));
    }

    @Transactional
    public void deleteById(UUID id) {
        CompanyPositions companyPositions = companyPositionsRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Cargo da empresa com id:" + id + " não encontrado(a) para exclusão"));

        UUID companyId = companyPositions.getCompany().getId();
        UUID positionId = companyPositions.getPosition().getId();

        rbac.requireOwnCompany(companyId);

        if (userCompanyRepository.existsByCompanyIdAndPositionId(companyId, positionId)) {
            throw new ResourceInUseException("Cargo não pode ser excluído(a): possui usuário(s) vinculado(s) na empresa");
        }

        companyPositionsRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public CompanyPositionsResponseDTO findById(UUID id) {
        CompanyPositions companyPositions = companyPositionsRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cargo da empresa não encontrado(a) com ID: " + id));
        return toResponse(companyPositions);
    }

    @Transactional(readOnly = true)
    public CompanyPositionsResponseDTO findById(UUID id, UUID companyId) {
        CompanyPositions companyPositions = companyPositionsRepository.findByIdAndCompanyId(id, companyId).orElseThrow(
                () -> new ResourceNotFoundException("Cargo da empresa não encontrado(a) com ID: " + id));
        return toResponse(companyPositions);
    }

    @Transactional(readOnly = true)
    public List<CompanyPositionsResponseDTO> findAllByCompany(UUID companyId) {
        return companyPositionsRepository.findByCompanyId(companyId).stream().map(this::toResponse).toList();
    }

    private CompanyPositionsResponseDTO toResponse(CompanyPositions entity) {
        CompanyPositionsResponseDTO response = objectMapper.convertValue(entity, CompanyPositionsResponseDTO.class);
        response.setCompanyId(entity.getCompany().getId());
        response.setPosition(objectMapper.convertValue(entity.getPosition(), PositionResponseDTO.class));
        return response;
    }
}
