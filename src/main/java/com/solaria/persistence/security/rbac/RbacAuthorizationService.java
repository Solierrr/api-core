package com.solaria.persistence.security.rbac;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.identity.Position;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.identity.PositionPermissionRepository;
import com.solaria.persistence.repository.identity.PlatformAdminRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.security.CurrentUserRequestContext;
import com.solaria.persistence.security.CurrentUserRequestContext.CompanyAccess;

 /**
 * Resolve a autorização do usuário, em 2 camadas:
 * Endpoint -> consulta se o cargo do usuário tem acesso ao endpoint
 * Empresa -> consulta se o usuário pertence a empresa-alvo da operação
 */
@Component("rbac")
public class RbacAuthorizationService {


    // Recursos pessoais exigem JWT válido, mas não vínculo ou permissão empresarial.
    static final Set<String> AUTHENTICATED_USER_ENDPOINTS = Set.of(
            "POST /api/professional-registrations",
            "GET /api/professional-registrations/{id}",
            "PUT /api/professional-registrations/{id}",
            "DELETE /api/professional-registrations/{id}",
            "GET /api/professional-registrations/technician/{technicianId}",
            "POST /api/certification-records",
            "GET /api/certification-records/{id}",
            "PUT /api/certification-records/{id}",
            "DELETE /api/certification-records/{id}",
            "GET /api/certification-records/professional-registration/{professionalRegistrationId}",
            "GET /api/professions",
            "GET /api/professions/{id}",
            "GET /api/certifications",
            "GET /api/certifications/{id}",
            "POST /api/access-codes/redeem",
            "GET /api/users/auth/{authId}/summary",
            "PATCH /api/users/me",
            "POST /api/persons",
            "GET /api/persons",
            "GET /api/persons/{id}",
            "PUT /api/persons/{id}",
            "DELETE /api/persons/{id}",
            "POST /api/technicians",
            "GET /api/technicians",
            "GET /api/technicians/{id}",
            "PUT /api/technicians/{id}",
            "DELETE /api/technicians/{id}",
            "PUT /api/users/{id}",
            "DELETE /api/users/{id}",
            "POST /api/flux-logs",
            "GET /api/flux-logs/user/{userId}",
            "POST /api/connections",
            "GET /api/connections",
            "GET /api/connections/{id}",
            "DELETE /api/connections/{id}",
            "GET /api/users/me",
            "GET /api/user-companies/me",
            "GET /api/user-companies/me/all",
            "GET /api/platform-admins/me",
            "GET /api/technical-services/me",
            "POST /api/technical-services/{id}/acceptance",
            "POST /api/technical-services/{id}/completion",
            "PUT /api/user-photos/user/{userId}/type/{type}",
            "DELETE /api/user-photos/user/{userId}/type/{type}",
            "GET /api/user-photos/user/{userId}",
            "POST /api/technician-affiliations",
            "PATCH /api/technician-affiliations/{id}/active",
            "PATCH /api/technician-affiliations/{id}/type",
            "GET /api/technician-affiliations/technician/{technicianId}",
            "GET /api/feeds/professionals",
            "GET /api/feeds/offers",
            "GET /api/feeds/suppliers"
    );

    // Compra de unidades: sem vínculo com empresa passa (a elegibilidade do profissional é checada no serviço);
    // com vínculo, vale a permissão do cargo. Empresa fornecedora só compra como profissional aprovado (serviço).
    private static final Set<String> PURCHASE_ENDPOINTS = Set.of("/api/cart", "/api/orders");

    // Catálogo público (sem JWT): só leitura.
    static final Set<String> PUBLIC_CATALOG_ENDPOINTS = Set.of(
            "GET /api/catalog/teams",
            "GET /api/catalog/technicians",
            "GET /api/catalog/technicians/{slug}",
            "GET /api/catalog/companies",
            "GET /api/catalog/companies/{slug}",
            "GET /api/catalog/offers",
            "GET /api/catalog/offers/{companySlug}/{offerSlug}"
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
        if ("POST /api/companies".equals(endpointIdentifier)) {
            currentUserService.getProvisionedCurrentUser();
            return;
        }
        if (endpointIdentifier.contains(" /api/projects") || endpointIdentifier.contains(" /api/professional-onboarding/")
                || endpointIdentifier.equals("GET /api/users/me/mobile-eligibility")
                || endpointIdentifier.equals("GET /api/professions/{id}/requirements")) {
            currentUserService.getCurrentUser();
            return;
        }
        if (endpointIdentifier.contains(" /api/professional-review/") || endpointIdentifier.contains(" /api/brand-review")
                || endpointIdentifier.equals("POST /api/platform-admins")
                || endpointIdentifier.equals("PATCH /api/companies/{id}/approval")
                || endpointIdentifier.equals("PATCH /api/companies/{id}/rejection")
                || endpointIdentifier.equals("POST /api/technicians/{id}/approval")
                || endpointIdentifier.equals("POST /api/technicians/{id}/rejection")) {
            if (!isPlatformAdmin()) throw new UnauthorizedAccessException("Operação restrita ao Admin Solaria");
            return;
        }
        if (PUBLIC_CATALOG_ENDPOINTS.contains(endpointIdentifier)) {
            return;
        }
        if (AUTHENTICATED_USER_ENDPOINTS.contains(endpointIdentifier)) {
            return;
        }
        if (isPurchaseEndpoint(endpointIdentifier) && findCurrentCompanyId().isEmpty()) {
            return;
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
        return currentUserService.findCurrentUserId()
                .map(platformAdminRepository::existsByUserId)
                .orElse(false);
    }

    // Checagem de permissão do usuário / ADMIN ignora isso por ter acesso a todos os Endpoints
    @Transactional(readOnly = true)
    public boolean hasEndpointAccess(String endpointIdentifier) {
        return resolveUserCompany()
                .map(access -> Position.ADMIN_NAME.equals(access.positionName())
                        || positionHasPermission(access.positionId(), endpointIdentifier))
                .orElse(false);
    }

    // checagem de empresa pertencente ao usuário
    @Transactional(readOnly = true)
    public void requireOwnCompany(UUID companyId) {
        boolean sameCompany = resolveUserCompany()
                .map(access -> access.companyId().equals(companyId))
                .orElse(false);
        if (!sameCompany) {
            throw new UnauthorizedAccessException("O objeto da operação não foi encontrado.");
        }
    }



    // checagem de usuário dono da entity -> apenas o próprio usuario pode alterar seus dados(admin não faz bypass)
    @Transactional(readOnly = true)
    public void requireOwnUser(UUID userId) {
        boolean isSelf = currentUserService.findCurrentUserId()
                .map(currentUserId -> currentUserId.equals(userId))
                .orElse(false);
        if (!isSelf) {
            throw new UnauthorizedAccessException("O objeto da operação não foi encontrado.");
        }
    }

    @Transactional(readOnly = true)
    public Optional<UUID> findCurrentCompanyId() {
        return resolveUserCompany().map(CompanyAccess::companyId);
    }

    @Transactional(readOnly = true)
    public UUID getCurrentCompanyId() {
        return resolveUserCompany().map(CompanyAccess::companyId).orElseThrow(
                () -> new UnauthorizedAccessException("Usuário sem vínculo empresarial"));
    }

    private Optional<CompanyAccess> resolveUserCompany() {
        return currentUserService.findCurrentUserId().flatMap(userId -> CurrentUserRequestContext.current()
                .map(context -> context.findCompanyAccess(() -> findCompanyAccess(userId)))
                .orElseGet(() -> findCompanyAccess(userId)));
    }

    private Optional<CompanyAccess> findCompanyAccess(UUID userId) {
        return userCompanyRepository.findByUserId(userId)
                .map(link -> new CompanyAccess(link.getCompany().getId(), link.getPosition().getId(),
                        link.getPosition().getName()));
    }

    private boolean positionHasPermission(UUID positionId, String permissionName) {
        return CurrentUserRequestContext.current()
                .map(context -> context.hasEndpointGrant(permissionName,
                        () -> positionPermissionRepository
                                .existsByPositionIdAndPermission_PermissionName(positionId, permissionName)))
                .orElseGet(() -> positionPermissionRepository
                        .existsByPositionIdAndPermission_PermissionName(positionId, permissionName));
    }
}
