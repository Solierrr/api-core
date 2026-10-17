package com.solaria.persistence.service.identity;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.identity.AccessCode;
import com.solaria.persistence.domain.entity.identity.Position;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.identity.UserCompany;
import com.solaria.persistence.domain.enums.identity.AccessCodeStatus;
import com.solaria.persistence.dto.request.identity.AccessCodeRequestDTO;
import com.solaria.persistence.dto.response.identity.AccessCodeResponseDTO;
import com.solaria.persistence.dto.response.identity.PositionResponseDTO;
import com.solaria.persistence.dto.response.identity.UserCompanyResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.company.CompanyPositionsRepository;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.identity.AccessCodeRepository;
import com.solaria.persistence.repository.identity.PositionRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

@Service
public class AccessCodeService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;
    private static final int EXPIRATION_DAYS = 1;

    private final AccessCodeRepository accessCodeRepository;
    private final CompanyRepository companyRepository;
    private final PositionRepository positionRepository;
    private final CompanyPositionsRepository companyPositionsRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final CurrentUserService currentUserService;
    private final RbacAuthorizationService rbac;
    private final ObjectMapper objectMapper;
    private final SecureRandom random = new SecureRandom();

    public AccessCodeService(AccessCodeRepository accessCodeRepository,
                              CompanyRepository companyRepository,
                              PositionRepository positionRepository,
                              CompanyPositionsRepository companyPositionsRepository,
                              UserCompanyRepository userCompanyRepository,
                              CurrentUserService currentUserService,
                              RbacAuthorizationService rbac,
                              ObjectMapper objectMapper) {
        this.accessCodeRepository = accessCodeRepository;
        this.companyRepository = companyRepository;
        this.positionRepository = positionRepository;
        this.companyPositionsRepository = companyPositionsRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.currentUserService = currentUserService;
        this.rbac = rbac;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AccessCodeResponseDTO generate(AccessCodeRequestDTO dto) {
        rbac.requireOwnCompany(dto.getCompanyId());

        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        Position position = positionRepository.findById(dto.getPositionId())
                .orElseThrow(() -> new ResourceNotFoundException("Cargo não encontrado"));

        if (!companyPositionsRepository.existsByCompanyIdAndPositionId(dto.getCompanyId(), dto.getPositionId())) {
            throw new InvalidFieldException("Cargo não disponível para a empresa");
        }

        AccessCode accessCode = new AccessCode();
        accessCode.setCompany(company);
        accessCode.setPosition(position);
        accessCode.setCode(generateUniqueCode());
        accessCode.setStatus(AccessCodeStatus.ACTIVE);
        accessCode.setExpiresAt(Instant.now().plus(EXPIRATION_DAYS, ChronoUnit.DAYS));

        return toResponse(accessCodeRepository.save(accessCode));
    }

    @Transactional
    public void revoke(UUID id, UUID companyId) {
        rbac.requireOwnCompany(companyId);

        AccessCode accessCode = accessCodeRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Código de acesso não encontrado"));
        accessCode.setStatus(AccessCodeStatus.REVOKED);
        accessCodeRepository.save(accessCode);
    }

    @Transactional(readOnly = true)
    public List<AccessCodeResponseDTO> findByCompany(UUID companyId) {
        rbac.requireOwnCompany(companyId);
        return accessCodeRepository.findByCompanyId(companyId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserCompanyResponseDTO redeem(String code) {
        AccessCode accessCode = accessCodeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Código de acesso não encontrado"));

        if (accessCode.getStatus() != AccessCodeStatus.ACTIVE) {
            throw new BusinessRuleException("Código de acesso não está mais ativo");
        }
        if (accessCode.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessRuleException("Código de acesso expirado");
        }

        User user = currentUserService.getCurrentUser();
        if (userCompanyRepository.existsByUserId(user.getId())) {
            throw new UnauthorizedAccessException("Usuário já possui vínculo com uma empresa");
        }

        UserCompany userCompany = new UserCompany();
        userCompany.setUser(user);
        userCompany.setCompany(accessCode.getCompany());
        userCompany.setPosition(accessCode.getPosition());
        userCompanyRepository.save(userCompany);

        accessCode.setStatus(AccessCodeStatus.USED);
        accessCodeRepository.save(accessCode);

        UserCompanyResponseDTO response = objectMapper.convertValue(userCompany, UserCompanyResponseDTO.class);
        response.setCompanyId(accessCode.getCompany().getId());
        response.setUserId(user.getId());
        response.setPosition(objectMapper.convertValue(accessCode.getPosition(), PositionResponseDTO.class));
        return response;
    }

    private String generateUniqueCode() {
        String candidate;
        do {
            StringBuilder builder = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                builder.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
            }
            candidate = builder.toString();
        } while (accessCodeRepository.existsByCode(candidate));
        return candidate;
    }

    private AccessCodeResponseDTO toResponse(AccessCode accessCode) {
        AccessCodeResponseDTO response = new AccessCodeResponseDTO();
        response.setId(accessCode.getId());
        response.setCompanyId(accessCode.getCompany().getId());
        response.setCode(accessCode.getCode());
        response.setStatus(accessCode.getStatus());
        response.setExpiresAt(accessCode.getExpiresAt());
        response.setPosition(objectMapper.convertValue(accessCode.getPosition(), PositionResponseDTO.class));
        return response;
    }
}
