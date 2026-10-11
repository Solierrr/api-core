package com.solaria.persistence.service.execution;

import java.util.List;
import java.util.UUID;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import com.solaria.persistence.mapper.company.CompanyResponseMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.execution.Requester;
import com.solaria.persistence.domain.enums.company.CompanyStatus;
import com.solaria.persistence.dto.request.execution.RequesterRequestDTO;
import com.solaria.persistence.dto.response.execution.RequesterResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.proposal.ProposalRepository;
import com.solaria.persistence.repository.execution.RequesterRepository;
import com.solaria.persistence.repository.catalog.SupplierRepository;
import com.solaria.persistence.repository.execution.TechnicalProjectRepository;

@Service
public class RequesterService {

    private final RbacAuthorizationService rbac;
    private final RequesterRepository requesterRepository;
    private final CompanyRepository companyRepository;
    private final SupplierRepository supplierRepository;
    private final ProposalRepository proposalRepository;
    private final TechnicalProjectRepository technicalProjectRepository;

    public RequesterService(RequesterRepository requesterRepository,
                            CompanyRepository companyRepository,
                            SupplierRepository supplierRepository,
                            ProposalRepository proposalRepository,
                            TechnicalProjectRepository technicalProjectRepository,
                            RbacAuthorizationService rbac) {
        this.rbac = rbac;
        this.requesterRepository = requesterRepository;
        this.companyRepository = companyRepository;
        this.supplierRepository = supplierRepository;
        this.proposalRepository = proposalRepository;
        this.technicalProjectRepository = technicalProjectRepository;
    }

    @Transactional
    public RequesterResponseDTO save(RequesterRequestDTO dto) {
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa não encontrada com ID: " + dto.getCompanyId()));

        rbac.requireOwnCompany(company.getId());

        if (company.getStatus() != CompanyStatus.APPROVED) {
            throw new BusinessRuleException(
                    "Empresa " + company.getId() + " não pode se tornar demandante: status " + company.getStatus());
        }

        if (supplierRepository.existsByCompanyId(dto.getCompanyId())) {
            throw new BusinessRuleException(
                    "Empresa " + dto.getCompanyId() + " já é fornecedora e não pode ser demandante");
        }

        if (requesterRepository.existsByCompanyId(dto.getCompanyId())) {
            throw new DuplicateResourceException(
                    "Demandante já cadastrado para a empresa: " + dto.getCompanyId());
        }

        Requester requester = new Requester();
        requester.setCompany(company);
        requester.setBusinessType(dto.getBusinessType());

        return toResponse(requesterRepository.save(requester), rbac.findCurrentCompanyId().orElse(null));
    }

    @Transactional
    public void deleteById(UUID id) {
        Requester resource = requesterRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(
                    "Demandante com id:" + id + " não encontrado(a) para exclusão"));
        rbac.requireOwnCompany(resource.getCompany().getId());

        if (proposalRepository.existsByRequesterId(id)) {
            throw new ResourceInUseException("Demandante não pode ser excluído(a): possui proposta vinculada(s)");
        }
        if (technicalProjectRepository.existsByRequesterId(id)) {
            throw new ResourceInUseException("Demandante não pode ser excluído(a): possui projeto técnico vinculado(s)");
        }

        requesterRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public RequesterResponseDTO findById(UUID id) {
        Requester requester = requesterRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Demandante não encontrado com ID: " + id));
        return toResponse(requester, rbac.findCurrentCompanyId().orElse(null));
    }

    @Transactional(readOnly = true)
    public RequesterResponseDTO findById(UUID id, UUID companyId) {
        Requester requester = requesterRepository.findByIdAndCompanyId(id, companyId).orElseThrow(
                () -> new ResourceNotFoundException("Demandante não encontrado com ID: " + id));
        return toResponse(requester, rbac.findCurrentCompanyId().orElse(null));
    }

    @Transactional(readOnly = true)
    public List<RequesterResponseDTO> findAll() {
        UUID actorCompanyId = rbac.findCurrentCompanyId().orElse(null);
        return requesterRepository.findAll().stream()
                .map(item -> toResponse(item, actorCompanyId)).toList();
    }

    @Transactional(readOnly = true)
    public List<RequesterResponseDTO> findByCompany(UUID companyId) {
        UUID actorCompanyId = rbac.findCurrentCompanyId().orElse(null);
        return requesterRepository.findByCompanyId(companyId).stream()
                .map(item -> toResponse(item, actorCompanyId)).toList();
    }

    private RequesterResponseDTO toResponse(Requester requester, UUID actorCompanyId) {
        RequesterResponseDTO dto = new RequesterResponseDTO();
        dto.setId(requester.getId());
        dto.setCompany(CompanyResponseMapper.toResponse(requester.getCompany(), actorCompanyId));
        dto.setBusinessType(requester.getBusinessType());
        return dto;
    }

}
