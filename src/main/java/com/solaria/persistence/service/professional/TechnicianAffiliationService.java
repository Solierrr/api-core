package com.solaria.persistence.service.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.professional.Technician;
import com.solaria.persistence.domain.entity.professional.TechnicianAffiliation;
import com.solaria.persistence.domain.enums.professional.TechnicalAffiliationType;
import com.solaria.persistence.dto.request.professional.TechnicianAffiliationRequestDTO;
import com.solaria.persistence.dto.response.professional.TechnicianAffiliationResponseDTO;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.professional.TechnicianAffiliationRepository;
import com.solaria.persistence.repository.professional.TechnicianRepository;

@Service
public class TechnicianAffiliationService {

    private final TechnicianAffiliationRepository technicianAffiliationRepository;
    private final CompanyRepository companyRepository;
    private final TechnicianRepository technicianRepository;
    private final ObjectMapper objectMapper;

    public TechnicianAffiliationService(TechnicianAffiliationRepository technicianAffiliationRepository,
                                        CompanyRepository companyRepository,
                                        TechnicianRepository technicianRepository,
                                        ObjectMapper objectMapper) {
        this.technicianAffiliationRepository = technicianAffiliationRepository;
        this.companyRepository = companyRepository;
        this.technicianRepository = technicianRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public TechnicianAffiliationResponseDTO save(TechnicianAffiliationRequestDTO dto) {
        Technician technician = technicianRepository.findById(dto.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Técnico não encontrado com ID: " + dto.getTechnicianId()));

        if (technicianAffiliationRepository.existsByTechnicianIdAndActiveTrue(dto.getTechnicianId())) {
            throw new DuplicateResourceException(
                    "Técnico já possui um vínculo ativo: desative-o antes de criar um novo vínculo");
        }

        Company company = null;
        if (dto.getCompanyId() != null) {
            company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Empresa não encontrada com ID: " + dto.getCompanyId()));
        }

        TechnicianAffiliation technicianAffiliation = new TechnicianAffiliation();
        technicianAffiliation.setCompany(company);
        technicianAffiliation.setTechnician(technician);
        technicianAffiliation.setAffiliationType(dto.getAffiliationType());
        technicianAffiliation.setActive(true);

        return toResponse(technicianAffiliationRepository.save(technicianAffiliation));
    }

    @Transactional
    public TechnicianAffiliationResponseDTO updateActive(UUID id, Boolean active) {
        TechnicianAffiliation technicianAffiliation = technicianAffiliationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vínculo Técnico-Empresa com id:" + id + " não encontrado para atualização"));

        technicianAffiliation.setActive(active);

        return toResponse(technicianAffiliationRepository.save(technicianAffiliation));
    }

    @Transactional
    public TechnicianAffiliationResponseDTO updateType(UUID id, TechnicalAffiliationType affiliationType) {
        TechnicianAffiliation technicianAffiliation = technicianAffiliationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vínculo Técnico-Empresa com id:" + id + " não encontrado para atualização"));

        technicianAffiliation.setAffiliationType(affiliationType);

        return toResponse(technicianAffiliationRepository.save(technicianAffiliation));
    }

    @Transactional(readOnly = true)
    public TechnicianAffiliationResponseDTO findById(UUID id) {
        TechnicianAffiliation technicianAffiliation = technicianAffiliationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vínculo Técnico-Empresa não encontrado com ID: " + id));
        return toResponse(technicianAffiliation);
    }

    @Transactional(readOnly = true)
    public TechnicianAffiliationResponseDTO findById(UUID id, UUID companyId) {
        TechnicianAffiliation technicianAffiliation = technicianAffiliationRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vínculo Técnico-Empresa não encontrado com ID: " + id));
        return toResponse(technicianAffiliation);
    }

    @Transactional(readOnly = true)
    public List<TechnicianAffiliationResponseDTO> findByTechnician(UUID technicianId) {
        return technicianAffiliationRepository.findByTechnicianId(technicianId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TechnicianAffiliationResponseDTO> findAllByCompany(UUID companyId) {
        return technicianAffiliationRepository.findByCompanyId(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    private TechnicianAffiliationResponseDTO toResponse(TechnicianAffiliation entity) {
        TechnicianAffiliationResponseDTO response = objectMapper.convertValue(entity, TechnicianAffiliationResponseDTO.class);
        response.setCompanyId(entity.getCompany() != null ? entity.getCompany().getId() : null);
        response.setTechnicianId(entity.getTechnician().getId());
        return response;
    }
}
