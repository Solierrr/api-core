package com.solaria.persistence.service.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.professional.Technician;
import com.solaria.persistence.domain.entity.professional.TechnicianAffiliation;
import com.solaria.persistence.domain.enums.professional.TechnicalAffiliationType;
import com.solaria.persistence.dto.request.professional.TechnicianAffiliationRequestDTO;
import com.solaria.persistence.dto.response.professional.TechnicianAffiliationResponseDTO;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.professional.TechnicianAffiliationRepository;
import com.solaria.persistence.repository.professional.TechnicianRepository;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

@Service
public class TechnicianAffiliationService {

    private final TechnicianAffiliationRepository technicianAffiliationRepository;
    private final CompanyRepository companyRepository;
    private final TechnicianRepository technicianRepository;
    private final CurrentUserService currentUserService;
    private final RbacAuthorizationService rbac;

    public TechnicianAffiliationService(TechnicianAffiliationRepository technicianAffiliationRepository,
                                        CompanyRepository companyRepository,
                                        TechnicianRepository technicianRepository,
                                        CurrentUserService currentUserService,
                                        RbacAuthorizationService rbac) {
        this.technicianAffiliationRepository = technicianAffiliationRepository;
        this.companyRepository = companyRepository;
        this.technicianRepository = technicianRepository;
        this.currentUserService = currentUserService;
        this.rbac = rbac;
    }

    @Transactional
    public TechnicianAffiliationResponseDTO save(TechnicianAffiliationRequestDTO dto) {
        currentUserService.getProvisionedCurrentUserId();
        Technician technician = findTechnician(dto.getTechnicianId());
        Company company = dto.getCompanyId() == null ? null : findCompany(dto.getCompanyId());
        requireAuthority(technician, company, "POST /api/technician-affiliations");
        validateType(dto.getAffiliationType(), company);

        if (technicianAffiliationRepository.existsByTechnicianIdAndActiveTrue(dto.getTechnicianId())) {
            throw new DuplicateResourceException(
                    "Técnico já possui um vínculo ativo: desative-o antes de criar um novo vínculo");
        }

        TechnicianAffiliation affiliation = new TechnicianAffiliation();
        affiliation.setCompany(company);
        affiliation.setTechnician(technician);
        affiliation.setAffiliationType(dto.getAffiliationType());
        affiliation.setActive(true);
        return toResponse(technicianAffiliationRepository.save(affiliation));
    }

    @Transactional
    public TechnicianAffiliationResponseDTO updateActive(UUID id, Boolean active) {
        currentUserService.getProvisionedCurrentUserId();
        TechnicianAffiliation affiliation = findAffiliation(id);
        requireAuthority(affiliation.getTechnician(), affiliation.getCompany(),
                "PATCH /api/technician-affiliations/{id}/active");
        affiliation.setActive(active);
        return toResponse(technicianAffiliationRepository.save(affiliation));
    }

    @Transactional
    public TechnicianAffiliationResponseDTO updateType(UUID id, TechnicalAffiliationType affiliationType) {
        currentUserService.getProvisionedCurrentUserId();
        TechnicianAffiliation affiliation = findAffiliation(id);
        requireAuthority(affiliation.getTechnician(), affiliation.getCompany(),
                "PATCH /api/technician-affiliations/{id}/type");
        validateType(affiliationType, affiliation.getCompany());
        affiliation.setAffiliationType(affiliationType);
        return toResponse(technicianAffiliationRepository.save(affiliation));
    }

    @Transactional(readOnly = true)
    public TechnicianAffiliationResponseDTO findById(UUID id, UUID companyId) {
        currentUserService.getProvisionedCurrentUserId();
        TechnicianAffiliation affiliation = findAffiliation(id);
        requireCompanyAccess(affiliation.getCompany() == null ? null : affiliation.getCompany().getId(),
                "GET /api/technician-affiliations/{id}/company/{companyId}");
        Company company = findCompany(companyId);
        rbac.requireOwnCompany(company.getId());
        if (affiliation.getCompany() == null || !affiliation.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Vínculo Técnico-Empresa não encontrado com ID: " + id);
        }
        return toResponse(affiliation);
    }

    @Transactional(readOnly = true)
    public List<TechnicianAffiliationResponseDTO> findByTechnician(UUID technicianId) {
        currentUserService.getProvisionedCurrentUserId();
        requireTechnicianOwner(findTechnician(technicianId));
        return technicianAffiliationRepository.findByTechnicianId(technicianId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TechnicianAffiliationResponseDTO> findAllByCompany(UUID companyId) {
        currentUserService.getProvisionedCurrentUserId();
        Company company = findCompany(companyId);
        requireCompanyAccess(company.getId(), "GET /api/technician-affiliations/company/{companyId}");
        return technicianAffiliationRepository.findByCompanyId(companyId).stream()
                .map(this::toResponse).toList();
    }

    private Technician findTechnician(UUID id) {
        return technicianRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Técnico não encontrado com ID: " + id));
    }

    private Company findCompany(UUID id) {
        return companyRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Empresa não encontrada com ID: " + id));
    }

    private TechnicianAffiliation findAffiliation(UUID id) {
        return technicianAffiliationRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Vínculo Técnico-Empresa não encontrado com ID: " + id));
    }

    private void requireAuthority(Technician technician, Company company, String endpoint) {
        if (company == null) {
            requireTechnicianOwner(technician);
        } else {
            requireCompanyAccess(company.getId(), endpoint);
        }
    }

    private void requireTechnicianOwner(Technician technician) {
        var user = technician.getPerson().getUser();
        rbac.requireOwnUser(user == null ? null : user.getId());
    }

    private void requireCompanyAccess(UUID companyId, String endpoint) {
        if (!rbac.hasEndpointAccess(endpoint)) {
            throw new UnauthorizedAccessException("Operação não autorizada para o cargo: " + endpoint);
        }
        rbac.requireOwnCompany(companyId);
    }

    private void validateType(TechnicalAffiliationType type, Company company) {
        if (type == null || (type == TechnicalAffiliationType.INDEPENDENT) != (company == null)) {
            throw new InvalidFieldException("INDEPENDENT exige empresa ausente; AFFILIATED/PARTNER exigem empresa.");
        }
    }

    private TechnicianAffiliationResponseDTO toResponse(TechnicianAffiliation entity) {
        TechnicianAffiliationResponseDTO response = new TechnicianAffiliationResponseDTO();
        response.setId(entity.getId());
        response.setCompanyId(entity.getCompany() == null ? null : entity.getCompany().getId());
        response.setTechnicianId(entity.getTechnician().getId());
        response.setAffiliationType(entity.getAffiliationType());
        response.setActive(entity.getActive());
        return response;
    }
}
