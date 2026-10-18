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
            "GET /api/user-companies/me/all",
            "POST /api/connections",
            "GET /api/connections",
            "GET /api/connections/{id}",
            "DELETE /api/connections/{id}",
            "POST /api/access-codes/redeem",
            "POST /api/contacts",
            "POST /api/addresses",
            "POST /api/business-contacts",
            "POST /api/persons",
            "POST /api/technicians",
            "POST /api/professional-registrations",
            "GET /api/professions",
            "GET /api/platform-admins/me"
    );

    static final Set<String> PUBLIC_CATALOG_ENDPOINTS = Set.of(
            "GET /api/catalog/teams",
            "GET /api/catalog/companies",
            "GET /api/catalog/companies/{slug}",
            "GET /api/catalog/offers",
            "GET /api/catalog/offers/{companySlug}/{offerSlug}",
            "GET /api/catalog/technicians",
            "GET /api/catalog/technicians/{slug}"
    );

    private static final Set<String> SUPPLIER_ENDPOINTS = Set.of(
            "/api/models", "/api/model-photos", "/api/offers", "/api/inventories", "/api/suppliers",
            "/api/sales", "/api/subscription"
    );

    // Compra de unidades: sem vínculo com empresa passa (a elegibilidade do profissional é checada no serviço);
    // com vínculo, vale a permissão do cargo. Empresa fornecedora só compra como profissional aprovado (serviço).
    private static final Set<String> PURCHASE_ENDPOINTS = Set.of("/api/cart", "/api/orders");

    private static final Set<String> DEMANDANT_ENDPOINTS = Set.of(
            "/api/requesters", "/api/local-units", "/api/local-unit-photos",
            "/api/unit-specifications", "/api/energy-bills"
    );

    // Aprovação/rejeição de empresas e modelos (catálogo compartilhado, não pertence a
    // nenhuma empresa) é exclusiva do Admin Solaria — nenhum admin de empresa comum
    // deve poder aprovar/rejeitar outra empresa ou um modelo que não é seu.
    static final Set<String> PLATFORM_ADMIN_ONLY_ENDPOINTS = Set.of(
            "GET /api/brand-review",
            "POST /api/brand-review/{id}/decision",
            "PATCH /api/companies/{id}/approval",
            "PATCH /api/companies/{id}/rejection",
            "POST /api/models/{id}/approval",
            "POST /api/models/{id}/rejection",
            "GET /api/technicians",
            "POST /api/technicians/{id}/approval",
            "POST /api/technicians/{id}/rejection",
            "POST /api/professions",
            "PUT /api/professions/{id}",
            "DELETE /api/professions/{id}"
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
        if (endpointIdentifier.contains(" /api/projects") || endpointIdentifier.contains(" /api/professional-onboarding/")
                || endpointIdentifier.equals("GET /api/users/me/mobile-eligibility")
                || endpointIdentifier.equals("GET /api/professions/{id}/requirements")) {
            currentUserService.getCurrentUser();
            return;
        }
        if (endpointIdentifier.contains(" /api/professional-review/")) {
            if (!isPlatformAdmin()) throw new UnauthorizedAccessException("Operação restrita ao Admin Solaria");
            return;
        }
        if (BOOTSTRAP_ALWAYS_OPEN.contains(endpointIdentifier) || PUBLIC_CATALOG_ENDPOINTS.contains(endpointIdentifier)) {
            return;
        }
        if (AUTHENTICATED_USER_ENDPOINTS.contains(endpointIdentifier)) {
            return;
        }
        if (PLATFORM_ADMIN_ONLY_ENDPOINTS.contains(endpointIdentifier)) {
            if (isPlatformAdmin()) {
                return;
            }
            throw new UnauthorizedAccessException("Operação restrita ao Admin Solaria: " + endpointIdentifier);
        }
        // Admin Solaria (platform_admin) tem acesso irrestrito, desacoplado de
        // qualquer vínculo empresa/cargo — ver fase 06 do plano de telas web.
        if (isPlatformAdmin()) {
            return;
        }
        if (BOOTSTRAP_WHEN_NO_COMPANY_LINK.contains(endpointIdentifier) && currentUserHasNoCompanyLink()) {
            return;
        }
        if (isPurchaseEndpoint(endpointIdentifier)) {
            if (currentUserHasNoCompanyLink() || hasEndpointAccess(endpointIdentifier)) {
                return;
            }
            throw new UnauthorizedAccessException("Operação não autorizado para o cargo: " + endpointIdentifier);
        }
        if (!hasEndpointAccess(endpointIdentifier)) {
            throw new UnauthorizedAccessException("Operação não autorizado para o cargo: " + endpointIdentifier);
        }
    }

    private boolean isPurchaseEndpoint(String endpointIdentifier) {
        String path = endpointIdentifier.substring(endpointIdentifier.indexOf(' ') + 1);
        return PURCHASE_ENDPOINTS.stream().anyMatch(prefix -> path.equals(prefix) || path.startsWith(prefix + "/"));
    }

    public boolean isPlatformAdmin() {
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
    public UUID getCurrentCompanyId() {
        return resolveUserCompany().map(link -> link.getCompany().getId())
                .orElseThrow(() -> new UnauthorizedAccessException("A operação exige vínculo com uma empresa"));
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
        var attributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        String selected = attributes instanceof org.springframework.web.context.request.ServletRequestAttributes servlet
                ? servlet.getRequest().getHeader("X-Company-Id") : null;
        if (selected != null && !selected.isBlank()) {
            final UUID companyId;
            try { companyId = UUID.fromString(selected); }
            catch (IllegalArgumentException error) { throw new UnauthorizedAccessException("Contexto de empresa inválido"); }
            return currentUserService.findCurrentUser()
                    .flatMap(user -> userCompanyRepository.findByUserIdAndCompanyId(user.getId(), companyId));
        }
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
