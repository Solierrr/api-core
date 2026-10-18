package com.solaria.persistence.service.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.dto.response.identity.MyMembershipResponseDTO;
import com.solaria.persistence.dto.response.identity.OperationalContextResponseDTO;
import com.solaria.persistence.dto.response.identity.PositionResponseDTO;
import com.solaria.persistence.dto.request.identity.UserCompanyRequestDTO;
import com.solaria.persistence.dto.response.identity.UserCompanyResponseDTO;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.identity.Position;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.identity.UserCompany;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.company.CompanyPositionsRepository;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.identity.PositionPermissionRepository;
import com.solaria.persistence.repository.identity.PositionRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.repository.identity.UserRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import com.solaria.persistence.security.CurrentUserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;


@Service
public class UserCompanyService {

    private static final String USER_COMPANY_NOT_FOUND = "Vínculo usuário-empresa não encontrado com ID: ";

    private final UserCompanyRepository userCompanyRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PositionRepository positionRepository;
    private final CompanyPositionsRepository companyPositionsRepository;
    private final RbacAuthorizationService rbac;
    private final PositionPermissionRepository positionPermissionRepository;
    private final CurrentUserService currentUserService;
    @PersistenceContext
    private EntityManager entityManager;

    public UserCompanyService(UserCompanyRepository userCompanyRepository,
                              UserRepository userRepository,
                              CompanyRepository companyRepository,
                              PositionRepository positionRepository,
                              CompanyPositionsRepository companyPositionsRepository,
                              RbacAuthorizationService rbac,
                              PositionPermissionRepository positionPermissionRepository,
                              CurrentUserService currentUserService) {
        this.userCompanyRepository = userCompanyRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.positionRepository = positionRepository;
        this.companyPositionsRepository = companyPositionsRepository;
        this.rbac = rbac;
        this.positionPermissionRepository = positionPermissionRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public UserCompanyResponseDTO save(UserCompanyRequestDTO dto) {
        Company company = lockActorCompany();
        rbac.requireOwnCompany(dto.getCompanyId());
        User user = userRepository.findById(dto.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUserId()));
        Position position = positionRepository.findById(dto.getPositionId()).orElseThrow(
                () -> new ResourceNotFoundException("Cargo não encontrado com ID: " + dto.getPositionId()));

        if (userCompanyRepository.existsByUserId(dto.getUserId())) {
            throw new DuplicateResourceException(
                    "Usuário já possui vínculo com uma empresa: só é permitido um vínculo por usuário");
        }

        if (!companyPositionsRepository.existsByCompanyIdAndPositionId(dto.getCompanyId(), dto.getPositionId())) {
            throw new UnauthorizedAccessException("Cargo não disponível para a empresa");
        }

        requireAdminAssignment(position, company.getId());

        UserCompany userCompany = new UserCompany();
        userCompany.setUser(user);
        userCompany.setCompany(company);
        userCompany.setPosition(position);

        return toResponse(userCompanyRepository.save(userCompany));
    }

    @Transactional
    public UserCompanyResponseDTO updatePosition(UUID id, UUID positionId) {
        Company company = lockActorCompany();
        UserCompany userCompany = loadMembershipForMutation(id);

        Position position = positionRepository.findById(positionId).orElseThrow(
                () -> new ResourceNotFoundException("Cargo não encontrado com ID: " + positionId));

        UUID companyId = userCompany.getCompany().getId();

        rbac.requireOwnCompany(companyId);
        requireAdminAssignment(position, company.getId());

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
        lockActorCompany();
        UserCompany userCompany = loadMembershipForMutation(id);

        UUID companyId = userCompany.getCompany().getId();

        rbac.requireOwnCompany(companyId);

        if (Position.ADMIN_NAME.equals(userCompany.getPosition().getName()) && isLastAdminOfCompany(companyId, id)) {
            throw new UnauthorizedAccessException("A empresa deve manter pelo menos um vínculo ADMIN");
        }

        userCompanyRepository.deleteById(id);
    }

    private UserCompany loadMembershipForMutation(UUID id) {
        UserCompany membership = userCompanyRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(USER_COMPANY_NOT_FOUND + id));
        try {
            entityManager.refresh(membership);
        } catch (EntityNotFoundException removed) {
            throw new ResourceNotFoundException(USER_COMPANY_NOT_FOUND + id);
        }
        return membership;
    }

    private boolean isLastAdminOfCompany(UUID companyId, UUID excludingId) {
        return !userCompanyRepository.existsByCompanyIdAndPosition_NameAndIdNot(
                companyId, Position.ADMIN_NAME, excludingId);
    }

    private Company lockActorCompany() {
        UUID companyId = rbac.getCurrentCompanyId();
        return companyRepository.findByIdForUpdate(companyId).orElseThrow(
                () -> new ResourceNotFoundException("Empresa não encontrada com ID: " + companyId));
    }

    private void requireAdminAssignment(Position position, UUID companyId) {
        if (!Position.ADMIN_NAME.equals(position.getName())) {
            return;
        }
        UUID actorId = currentUserService.findCurrentUserId().orElseThrow(
                () -> new UnauthorizedAccessException("Usuário sem vínculo empresarial"));
        if (!userCompanyRepository.existsByUserIdAndCompanyIdAndPosition_Name(
                actorId, companyId, Position.ADMIN_NAME)) {
            throw new UnauthorizedAccessException("Somente ADMIN da própria empresa pode atribuir ADMIN");
        }
    }

    @Transactional(readOnly = true)
    public UserCompanyResponseDTO findById(UUID id) {
        UserCompany userCompany = userCompanyRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(USER_COMPANY_NOT_FOUND + id));
        rbac.requireOwnCompany(userCompany.getCompany().getId());
        return toResponse(userCompany);
    }

    @Transactional(readOnly = true)
    public UserCompanyResponseDTO findById(UUID id, UUID companyId) {
        rbac.requireOwnCompany(companyId);
        UserCompany userCompany = userCompanyRepository.findByIdAndCompanyId(id, companyId).orElseThrow(
                () -> new ResourceNotFoundException(USER_COMPANY_NOT_FOUND + id));
        return toResponse(userCompany);
    }

    @Transactional(readOnly = true)
    public List<UserCompanyResponseDTO> findAllByCompany(UUID companyId) {
        rbac.requireOwnCompany(companyId);
        return userCompanyRepository.findByCompanyId(companyId).
                stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserCompanyResponseDTO> findByUser(UUID userId) {
        return userCompanyRepository.findByUserId(userId)
                .stream()
                .map(membership -> {
                    rbac.requireOwnCompany(membership.getCompany().getId());
                    return toResponse(membership);
                })
                .toList();
    }

    /**
     * Vínculos do usuário autenticado. Hoje cada usuário tem no máximo um vínculo, mas o contrato
     * é uma lista para o web-app não depender dessa regra.
     */
    @Transactional(readOnly = true)
    public List<MyMembershipResponseDTO> findMyMemberships() {
        return findMyMembership().map(this::toMembership).stream().toList();
    }

    /** Contexto operacional do usuário autenticado, ou {@code null} quando ele não tem empresa. */
    @Transactional(readOnly = true)
    public OperationalContextResponseDTO findMyContext() {
        return findMyMembership().map(this::toContext).orElse(null);
    }

    private java.util.Optional<UserCompany> findMyMembership() {
        return currentUserService.findCurrentUserId().flatMap(userCompanyRepository::findByUserId);
    }

    private MyMembershipResponseDTO toMembership(UserCompany membership) {
        Position position = membership.getPosition();
        return new MyMembershipResponseDTO(
                membership.getCompany().getId(),
                new MyMembershipResponseDTO.PositionSummary(position.getId(), position.getName()),
                permissionsOf(position));
    }

    private OperationalContextResponseDTO toContext(UserCompany membership) {
        Company company = membership.getCompany();
        Position position = membership.getPosition();
        return new OperationalContextResponseDTO(
                company.getId(),
                company.getType(),
                company.getStatus(),
                position.getName(),
                Position.ADMIN_NAME.equals(position.getName()),
                permissionsOf(position));
    }

    // ADMIN passa em todo endpoint sem depender de grants (ver RbacAuthorizationService), então
    // a lista dele vai vazia: o web-app trata o ADMIN pelo nome do cargo.
    private List<String> permissionsOf(Position position) {
        if (Position.ADMIN_NAME.equals(position.getName())) {
            return List.of();
        }
        return positionPermissionRepository.findByPositionId(position.getId()).stream()
                .map(grant -> grant.getPermission().getPermissionName())
                .sorted()
                .toList();
    }

    // Monta o DTO à mão: converter a entidade com o ObjectMapper segue as relações bidirecionais
    // (UserCompany -> Company -> ...) e estoura a pilha com "Infinite recursion".
    private UserCompanyResponseDTO toResponse(UserCompany entity) {
        UserCompanyResponseDTO response = new UserCompanyResponseDTO();
        response.setId(entity.getId());
        response.setCompanyId(entity.getCompany().getId());
        response.setUserId(entity.getUser().getId());
        PositionResponseDTO position = new PositionResponseDTO();
        position.setId(entity.getPosition().getId());
        position.setName(entity.getPosition().getName());
        position.setCompanyId(entity.getCompany().getId());
        response.setPosition(position);
        return response;
    }
}
