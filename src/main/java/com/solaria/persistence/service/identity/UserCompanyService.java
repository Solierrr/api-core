package com.solaria.persistence.service.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;
import com.solaria.persistence.dto.response.identity.PositionResponseDTO;
import com.solaria.persistence.dto.request.identity.UserCompanyRequestDTO;
import com.solaria.persistence.dto.response.identity.UserCompanyResponseDTO;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.identity.Position;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.identity.UserCompany;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.company.CompanyPositionsRepository;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.identity.PositionRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.repository.identity.UserRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;


@Service
public class UserCompanyService {

    private final UserCompanyRepository userCompanyRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PositionRepository positionRepository;
    private final CompanyPositionsRepository companyPositionsRepository;
    private final RbacAuthorizationService rbac;
    private final ObjectMapper objectMapper;

    public UserCompanyService(UserCompanyRepository userCompanyRepository,
                              UserRepository userRepository,
                              CompanyRepository companyRepository,
                              PositionRepository positionRepository,
                              CompanyPositionsRepository companyPositionsRepository,
                              RbacAuthorizationService rbac,
                              ObjectMapper objectMapper) {
        this.userCompanyRepository = userCompanyRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.positionRepository = positionRepository;
        this.companyPositionsRepository = companyPositionsRepository;
        this.rbac = rbac;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UserCompanyResponseDTO save(UserCompanyRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUserId()));
        Company company = companyRepository.findById(dto.getCompanyId()).orElseThrow(
                () -> new ResourceNotFoundException("Empresa não encontrada com ID: " + dto.getCompanyId()));
        Position position = positionRepository.findById(dto.getPositionId()).orElseThrow(
                () -> new ResourceNotFoundException("Cargo não encontrado com ID: " + dto.getPositionId()));

        if (userCompanyRepository.existsByUserId(dto.getUserId())) {
            throw new DuplicateResourceException(
                    "Usuário já possui vínculo com uma empresa: só é permitido um vínculo por usuário");
        }

        if (!companyPositionsRepository.existsByCompanyIdAndPositionId(dto.getCompanyId(), dto.getPositionId())) {
            throw new UnauthorizedAccessException("Cargo não disponível para a empresa");
        }

        if (!userCompanyRepository.existsByCompanyId(dto.getCompanyId())) {
            if (!Position.ADMIN_NAME.equals(position.getName())) {
                throw new InvalidFieldException("A primeira conta de uma empresa deve ter o cargo ADMIN");
            }
        } else {
            rbac.requireOwnCompany(dto.getCompanyId());
        }

        UserCompany userCompany = new UserCompany();
        userCompany.setUser(user);
        userCompany.setCompany(company);
        userCompany.setPosition(position);

        return toResponse(userCompanyRepository.save(userCompany));
    }

    @Transactional
    public UserCompanyResponseDTO updatePosition(UUID id, UUID positionId) {
        UserCompany userCompany = userCompanyRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Vínculo usuário-empresa com id:" + id + " não encontrado(a) para atualização"));

        Position position = positionRepository.findById(positionId).orElseThrow(
                () -> new ResourceNotFoundException("Cargo não encontrado com ID: " + positionId));

        UUID companyId = userCompany.getCompany().getId();

        rbac.requireOwnCompany(companyId);

        if (!companyPositionsRepository.existsByCompanyIdAndPositionId(companyId, positionId)) {
            throw new UnauthorizedAccessException("Cargo não disponível para a empresa");
        }

        boolean leavingAdmin = Position.ADMIN_NAME.equals(userCompany.getPosition().getName())
                && !Position.ADMIN_NAME.equals(position.getName());
        if (leavingAdmin && isLastAdminOfCompany(companyId, id)) {
            throw new UnauthorizedAccessException("A empresa deve manter pelo menos um vínculo ADMIN");
        }

        userCompany.setPosition(position);

        return toResponse(userCompanyRepository.save(userCompany));
    }

    @Transactional
    public void deleteById(UUID id) {
        UserCompany userCompany = userCompanyRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Vínculo usuário-empresa com id:" + id + " não encontrado para exclusão"));

        UUID companyId = userCompany.getCompany().getId();

        rbac.requireOwnCompany(companyId);

        if (Position.ADMIN_NAME.equals(userCompany.getPosition().getName()) && isLastAdminOfCompany(companyId, id)) {
            throw new UnauthorizedAccessException("A empresa deve manter pelo menos um vínculo ADMIN");
        }

        userCompanyRepository.deleteById(id);
    }

    private boolean isLastAdminOfCompany(UUID companyId, UUID excludingId) {
        return userCompanyRepository.findByCompanyId(companyId).stream()
                .filter(uc -> !uc.getId().equals(excludingId))
                .noneMatch(uc -> Position.ADMIN_NAME.equals(uc.getPosition().getName()));
    }

    @Transactional(readOnly = true)
    public UserCompanyResponseDTO findById(UUID id) {
        UserCompany userCompany = userCompanyRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Vínculo usuário-empresa não encontrado com ID: " + id));
        return toResponse(userCompany);
    }

    @Transactional(readOnly = true)
    public UserCompanyResponseDTO findById(UUID id, UUID companyId) {
        UserCompany userCompany = userCompanyRepository.findByIdAndCompanyId(id, companyId).orElseThrow(
                () -> new ResourceNotFoundException("Vínculo usuário-empresa não encontrado com ID: " + id));
        return toResponse(userCompany);
    }

    @Transactional(readOnly = true)
    public List<UserCompanyResponseDTO> findAllByCompany(UUID companyId) {
        return userCompanyRepository.findByCompanyId(companyId).
                stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserCompanyResponseDTO> findByUser(UUID userId) {
        return userCompanyRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UserCompanyResponseDTO toResponse(UserCompany entity) {
        UserCompanyResponseDTO response = objectMapper.convertValue(entity, UserCompanyResponseDTO.class);
        response.setCompanyId(entity.getCompany().getId());
        response.setUserId(entity.getUser().getId());
        response.setPosition(objectMapper.convertValue(entity.getPosition(), PositionResponseDTO.class));
        return response;
    }
}
