package com.solaria.persistence.security.rbac;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.identity.Position;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.identity.UserCompany;
import com.solaria.persistence.domain.enums.company.CompanyType;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.identity.PlatformAdminRepository;
import com.solaria.persistence.repository.identity.PositionPermissionRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.security.CurrentUserService;

 /**
 * Resolve a autorização do usuário, em 2 camadas:
 * Endpoint -> consulta se o cargo do usuário tem acesso ao endpoint
 * Empresa -> consulta se o usuário pertence a empresa-alvo da operação
 */
@Component("rbac")
public class RbacAuthorizationService {


    // Endpoints liberados do estado do usuário
    static final Set<String> BOOTSTRAP_ALWAYS_OPEN = Set.of(
            "POST /api/companies"
    );

    // endpoints liberados enquanto o usuário não ter nenhum relacionamento UserCompany
    static final Set<String> BOOTSTRAP_WHEN_NO_COMPANY_LINK = Set.of();

    // Recursos pessoais exigem JWT válido, mas não vínculo ou permissão empresarial.
    static final Set<String> AUTHENTICATED_USER_ENDPOINTS = Set.of(
            "GET /api/companies/{id}/contact-user",
            "GET /api/users/auth/{authId}/summary",
            "GET /api/users/me",
            "PATCH /api/users/me",
            "GET /api/user-companies/me",
            "POST /api/connections",
            "GET /api/connections",
            "GET /api/connections/{id}",
            "DELETE /api/connections/{id}",
            "POST /api/access-codes/redeem",
            "GET /api/platform-admins/me"
    );

    static final Set<String> PUBLIC_CATALOG_ENDPOINTS = Set.of(
            "GET /api/catalog/companies",
            "GET /api/catalog/companies/{slug}",
            "GET /api/catalog/offers",
            "GET /api/catalog/offers/{companySlug}/{offerSlug}",
            "GET /api/catalog/technicians",
            "GET /api/catalog/technicians/{slug}"
    );

    private static final Set<String> SUPPLIER_ENDPOINTS = Set.of(
            "/api/models", "/api/model-photos", "/api/offers", "/api/inventories", "/api/suppliers"
    );

    private static final Set<String> DEMANDANT_ENDPOINTS = Set.of(
            "/api/requesters", "/api/local-units", "/api/local-unit-photos",
            "/api/unit-specifications", "/api/energy-bills"
    );

    private final CurrentUserService currentUserService;
    private final UserCompanyRepository userCompanyRepository;
    private final PositionPermissionRepository positionPermissionRepository;
    private final PlatformAdminRepository platformAdminRepository;

    public RbacAuthorizationService(CurrentUserService currentUserService,
                                     UserCompanyRepository userCompanyRepository,
                                     PositionPermissionRepository positionPermissionRepository,
                                     PlatformAdminRepository platformAdminRepository) {
        this.currentUserService = currentUserService;
        this.userCompanyRepository = userCompanyRepository;
        this.positionPermissionRepository = positionPermissionRepository;
        this.platformAdminRepository = platformAdminRepository;
    }

    // Lógica de RBAC
    @Transactional(readOnly = true)
    public void requireEndpointAccess(String endpointIdentifier) {
        if (BOOTSTRAP_ALWAYS_OPEN.contains(endpointIdentifier) || PUBLIC_CATALOG_ENDPOINTS.contains(endpointIdentifier)) {
            return;
        }
        if (AUTHENTICATED_USER_ENDPOINTS.contains(endpointIdentifier)) {
            return;
        }
        // Admin Solaria (platform_admin) tem acesso irrestrito, desacoplado de
        // qualquer vínculo empresa/cargo — ver fase 06 do plano de telas web.
        if (isPlatformAdmin()) {
            return;
        }
        if (BOOTSTRAP_WHEN_NO_COMPANY_LINK.contains(endpointIdentifier) && currentUserHasNoCompanyLink()) {
            return;
        }
        if (!hasEndpointAccess(endpointIdentifier)) {
            throw new UnauthorizedAccessException("Operação não autorizado para o cargo: " + endpointIdentifier);
        }
    }

    private boolean isPlatformAdmin() {
        return currentUserService.findCurrentUser()
                .map(user -> platformAdminRepository.existsByUserId(user.getId()))
                .orElse(false);
    }

    // Checagem de permissão do usuário / ADMIN ignora isso por ter acesso a todos os Endpoints
    @Transactional(readOnly = true)
    public boolean hasEndpointAccess(String endpointIdentifier) {
        return resolveUserCompany()
                .map(uc -> endpointMatchesCompanyType(uc.getCompany().getType(), endpointIdentifier)
                        && (isAdmin(uc.getPosition())
                            || positionHasPermission(uc.getPosition().getId(), endpointIdentifier)))
                .orElse(false);
    }

    private boolean endpointMatchesCompanyType(CompanyType type, String endpointIdentifier) {
        String path = endpointIdentifier.substring(endpointIdentifier.indexOf(' ') + 1);
        if (SUPPLIER_ENDPOINTS.stream().anyMatch(prefix -> path.equals(prefix) || path.startsWith(prefix + "/"))) {
            return type == CompanyType.SUPPLIER;
        }
        if (DEMANDANT_ENDPOINTS.stream().anyMatch(prefix -> path.equals(prefix) || path.startsWith(prefix + "/"))) {
            return type == CompanyType.DEMANDANT;
        }
        return true;
    }

    // checagem de empresa pertencente ao usuário
    @Transactional(readOnly = true)
    public void requireOwnCompany(UUID companyId) {
        boolean sameCompany = resolveUserCompany()
                .map(uc -> uc.getCompany().getId().equals(companyId))
                .orElse(false);
        if (!sameCompany) {
            throw new UnauthorizedAccessException("O objeto da operação não foi encontrado.");
        }
    }

    @Transactional(readOnly = true)
    public boolean currentUserHasNoCompanyLink() {
        return resolveUserCompany().isEmpty();
    }


    // checagem de usuário dono da entity -> apenas o próprio usuario pode alterar seus dados(admin não faz bypass)
    @Transactional(readOnly = true)
    public void requireOwnUser(UUID userId) {
        boolean isSelf = currentUserService.findCurrentUser()
                .map(user -> user.getId().equals(userId))
                .orElse(false);
        if (!isSelf) {
            throw new UnauthorizedAccessException("O objeto da operação não foi encontrado.");
        }
    }

    private Optional<UserCompany> resolveUserCompany() {
        return currentUserService.findCurrentUser()
                .flatMap(user -> userCompanyRepository.findByUserId(user.getId()));
    }

    private boolean isAdmin(Position position) {
        return Position.ADMIN_NAME.equals(position.getName());
    }

    private boolean positionHasPermission(UUID positionId, String permissionName) {
        return positionPermissionRepository.existsByPositionIdAndPermission_PermissionName(positionId, permissionName);
    }
}
