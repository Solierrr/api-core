package com.solaria.persistence.service.company;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.shared.Address;
import com.solaria.persistence.domain.entity.company.BusinessContact;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.enums.company.CompanyStatus;
import com.solaria.persistence.dto.request.company.CompanyCreateRequestDTO;
import com.solaria.persistence.dto.request.company.CompanyUpdateRequestDTO;
import com.solaria.persistence.dto.request.company.BusinessContactRequestDTO;
import com.solaria.persistence.dto.request.shared.AddressRequestDTO;
import com.solaria.persistence.domain.entity.identity.Position;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.identity.UserCompany;
import com.solaria.persistence.domain.entity.company.CompanyPositions;
import com.solaria.persistence.repository.identity.PositionRepository;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.dto.response.company.CompanyResponseDTO;
import com.solaria.persistence.mapper.company.CompanyResponseMapper;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.shared.AddressRepository;
import com.solaria.persistence.repository.company.BusinessContactRepository;
import com.solaria.persistence.repository.company.CompanyPhotoRepository;
import com.solaria.persistence.repository.company.CompanyPositionsRepository;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.execution.RequesterRepository;
import com.solaria.persistence.repository.catalog.SupplierRepository;
import com.solaria.persistence.repository.company.CompanyPlansRepository;
import com.solaria.persistence.repository.company.SubscriptionRepository;
import com.solaria.persistence.domain.entity.catalog.Supplier;
import com.solaria.persistence.domain.entity.company.CompanyPlans;
import com.solaria.persistence.domain.entity.company.Subscription;
import com.solaria.persistence.domain.entity.execution.Requester;
import com.solaria.persistence.domain.enums.company.CompanyType;
import java.time.OffsetDateTime;
import com.solaria.persistence.repository.professional.TechnicianAffiliationRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.util.SlugUtil;

@Service
public class CompanyService {

    private static final String DEFAULT_PLAN_NAME = "Gratuito";
    private static final String COMPANY_NOT_FOUND = "Empresa não encontrada com ID: ";

    private final CurrentUserService currentUserService;
    private final PositionRepository positionRepository;
    private final RbacAuthorizationService rbac;
    private final CompanyRepository companyRepository;
    private final AddressRepository addressRepository;
    private final BusinessContactRepository businessContactRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final SupplierRepository supplierRepository;
    private final RequesterRepository requesterRepository;
    private final TechnicianAffiliationRepository technicianAffiliationRepository;
    private final CompanyPositionsRepository companyPositionsRepository;
    private final CompanyPhotoRepository companyPhotoRepository;
    private final CompanyPlansRepository companyPlansRepository;
    private final SubscriptionRepository subscriptionRepository;

    public CompanyService(CompanyRepository companyRepository,
                          AddressRepository addressRepository,
                          BusinessContactRepository businessContactRepository,
                          UserCompanyRepository userCompanyRepository,
                          SupplierRepository supplierRepository,
                          RequesterRepository requesterRepository,
                          TechnicianAffiliationRepository technicianAffiliationRepository,
                          CompanyPositionsRepository companyPositionsRepository,
                          CompanyPhotoRepository companyPhotoRepository,
                          CompanyPlansRepository companyPlansRepository,
                          SubscriptionRepository subscriptionRepository,
                          CurrentUserService currentUserService,
                          PositionRepository positionRepository,
                          RbacAuthorizationService rbac) {
        this.currentUserService = currentUserService;
        this.positionRepository = positionRepository;
        this.rbac = rbac;
        this.companyRepository = companyRepository;
        this.addressRepository = addressRepository;
        this.businessContactRepository = businessContactRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.supplierRepository = supplierRepository;
        this.requesterRepository = requesterRepository;
        this.technicianAffiliationRepository = technicianAffiliationRepository;
        this.companyPositionsRepository = companyPositionsRepository;
        this.companyPhotoRepository = companyPhotoRepository;
        this.companyPlansRepository = companyPlansRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public CompanyResponseDTO save(CompanyCreateRequestDTO dto) {
        User actor = currentUserService.getProvisionedCurrentUser();
        if (userCompanyRepository.existsByUserId(actor.getId())) {
            throw new DuplicateResourceException("Usuário já possui vínculo com uma empresa");
        }
        if (companyRepository.existsByCnpj(dto.getCnpj())) {
            throw new DuplicateResourceException("Empresa já cadastrada para o CNPJ: " + dto.getCnpj());
        }
        Company company = new Company();
        company.setStatus(CompanyStatus.APPROVED);
        company.setCnpj(dto.getCnpj());
        company.setTradeName(dto.getTradeName());
        company.setCorporateName(dto.getCorporateName());
        company.setSlug(generateUniqueSlug(dto.getTradeName()));
        company.setType(dto.getType());
        companyRepository.saveAndFlush(company);
        createCompanyProfile(company);
        if (dto.getAddress() != null) {
            company.setAddress(createAddress(company, dto.getAddress()));
        }
        updateBusinessContact(company, dto.getBusinessContact());
        Position admin = new Position();
        admin.setName(Position.ADMIN_NAME);
        positionRepository.save(admin);
        CompanyPositions companyPosition = new CompanyPositions();
        companyPosition.setCompany(company);
        companyPosition.setPosition(admin);
        companyPositionsRepository.save(companyPosition);
        UserCompany membership = new UserCompany();
        membership.setUser(actor);
        membership.setCompany(company);
        membership.setPosition(admin);
        userCompanyRepository.saveAndFlush(membership);
        return CompanyResponseMapper.toResponse(company, company.getId());
    }

    private void createCompanyProfile(Company company) {
        if (company.getType() == CompanyType.SUPPLIER) {
            Supplier supplier = new Supplier();
            supplier.setCompany(company);
            Supplier savedSupplier = supplierRepository.save(supplier);

            // Assinatura no plano gratuito, pré-requisito para criar ofertas
            CompanyPlans defaultPlan = companyPlansRepository.findByName(DEFAULT_PLAN_NAME)
                    .orElseThrow(() -> new ResourceNotFoundException("Plano padrão não configurado"));
            Subscription subscription = new Subscription();
            subscription.setSupplier(savedSupplier);
            subscription.setPlan(defaultPlan);
            subscription.setStartDate(OffsetDateTime.now());
            subscriptionRepository.save(subscription);
        } else {
            Requester requester = new Requester();
            requester.setCompany(company);
            requesterRepository.save(requester);
        }
    }

    @Transactional
    public CompanyResponseDTO update(UUID id, CompanyUpdateRequestDTO dto) {
        rbac.requireOwnCompany(id);
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(COMPANY_NOT_FOUND + id));
        company.setAddress(resolveAddress(dto.getAddressId(), id));
        updateBusinessContact(company, dto.getBusinessContact());
        company.setTradeName(dto.getTradeName());
        company.setCorporateName(dto.getCorporateName());
        return CompanyResponseMapper.toResponse(companyRepository.save(company), id);
    }

    private Address createAddress(Company company, AddressRequestDTO dto) {
        Address address = new Address();
        address.setOwnerCompany(company);
        address.setState(dto.getState());
        address.setCity(dto.getCity());
        address.setNeighborhood(dto.getNeighborhood());
        address.setZipCode(dto.getZipCode());
        address.setStreet(dto.getStreet());
        address.setNumber(dto.getNumber());
        return addressRepository.saveAndFlush(address);
    }

    private void updateBusinessContact(Company company, BusinessContactRequestDTO dto) {
        BusinessContact contact = company.getBusinessContact();
        if (dto == null) {
            company.setBusinessContact(null);
            if (contact != null) {
                companyRepository.saveAndFlush(company);
                businessContactRepository.delete(contact);
            }
            return;
        }
        if (contact == null) {
            contact = new BusinessContact();
        }
        contact.setCompanyEmail(dto.getCompanyEmail());
        contact.setPhone(dto.getPhone());
        contact.setWebsite(dto.getWebsite());
        company.setBusinessContact(businessContactRepository.saveAndFlush(contact));
    }

    @Transactional
    public CompanyResponseDTO attachAddress(UUID companyId, UUID addressId) {
        rbac.requireOwnCompany(companyId);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(COMPANY_NOT_FOUND + companyId));
        company.setAddress(resolveAddress(addressId, companyId));
        return CompanyResponseMapper.toResponse(companyRepository.save(company), companyId);
    }

    @Transactional
    public void deleteById(UUID id) {
        Company resource = companyRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(
                    "Empresa com id:" + id + " não encontrada para exclusão"));
        rbac.requireOwnCompany(resource.getId());

        if (userCompanyRepository.existsByCompanyId(id)
                || supplierRepository.existsByCompanyId(id)
                || requesterRepository.existsByCompanyId(id)
                || technicianAffiliationRepository.existsByCompanyId(id)
                || companyPositionsRepository.existsByCompanyId(id)
                || companyPhotoRepository.existsByCompanyId(id)) {
            throw new ResourceInUseException(
                    "Empresa não pode ser excluída: possui vínculo(s) associado(s)");
        }

        companyRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public CompanyResponseDTO findById(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        COMPANY_NOT_FOUND + id));
        return CompanyResponseMapper.toResponse(company, rbac.findCurrentCompanyId().orElse(null));
    }

    @Transactional(readOnly = true)
    public List<CompanyResponseDTO> findAll() {
        UUID actorCompanyId = rbac.findCurrentCompanyId().orElse(null);
        return companyRepository.findAll().stream().map(company -> CompanyResponseMapper.toResponse(company, actorCompanyId)).toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponseDTO findByCnpj(String cnpj) {
        String normalized = normalizeCnpj(cnpj);
        Company company = companyRepository.findByCnpj(normalized)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa não encontrada com CNPJ: " + normalized));
        return CompanyResponseMapper.toResponse(company, rbac.findCurrentCompanyId().orElse(null));
    }

    private String normalizeCnpj(String cnpj) {
        return cnpj == null ? null : cnpj.replaceAll("\\D", "");
    }

    private Address resolveAddress(UUID addressId, UUID companyId) {
        if (addressId == null) {
            return null;
        }
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado com ID: " + addressId));
        if (!companyId.equals(address.getOwnerCompany().getId())) {
            throw new UnauthorizedAccessException("O objeto da operação não foi encontrado.");
        }
        return address;
    }

    private String generateUniqueSlug(String tradeName) {
        String base = SlugUtil.slugify(tradeName);
        String candidate = base;
        int suffix = 2;
        while (companyRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

}
