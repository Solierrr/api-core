package com.solaria.persistence.security.rbac;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.identity.Position;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.identity.UserCompany;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.identity.PositionPermissionRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.repository.identity.UserRepository;

 /**
 * Resolve a autorização do usuário, em 2 camadas:
 * Endpoint -> consulta se o cargo do usuário tem acesso ao endpoint
 * Empresa -> consulta se o usuário pertence a empresa-alvo da operação
 */
@Component("rbac")
public class RbacAuthorizationService {


    // Endpoints liberados do estado do usuário
    static final Set<String> BOOTSTRAP_ALWAYS_OPEN = Set.of(
            "POST /api/users",
            "POST /api/companies"
    );

    // endpoints liberados enquanto o usuário não ter nenhum relacionamento UserCompany
    static final Set<String> BOOTSTRAP_WHEN_NO_COMPANY_LINK = Set.of(
            "POST /api/company-positions",
            "POST /api/user-companies"
    );

    private final UserRepository userRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final PositionPermissionRepository positionPermissionRepository;

    public RbacAuthorizationService(UserRepository userRepository,
                                     UserCompanyRepository userCompanyRepository,
                                     PositionPermissionRepository positionPermissionRepository) {
        this.userRepository = userRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.positionPermissionRepository = positionPermissionRepository;
    }

    // Lógica de RBAC
    @Transactional(readOnly = true)
    public void requireEndpointAccess(String endpointIdentifier) {
        if (BOOTSTRAP_ALWAYS_OPEN.contains(endpointIdentifier)) {
            return;
        }
        if (BOOTSTRAP_WHEN_NO_COMPANY_LINK.contains(endpointIdentifier) && currentUserHasNoCompanyLink()) {
            return;
        }
        if (!hasEndpointAccess(endpointIdentifier)) {
            throw new UnauthorizedAccessException("Operação não autorizado para o cargo: " + endpointIdentifier);
        }
    }

    // Checagem de permissão do usuário / ADMIN ignora isso por ter acesso a todos os Endpoints
    @Transactional(readOnly = true)
    public boolean hasEndpointAccess(String endpointIdentifier) {
        return resolveUserCompany()
                .map(uc -> isAdmin(uc.getPosition())
                        || positionHasPermission(uc.getPosition().getId(), endpointIdentifier))
                .orElse(false);
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
        boolean isSelf = currentUser().map(user -> user.getId().equals(userId)).orElse(false);
        if (!isSelf) {
            throw new UnauthorizedAccessException("O objeto da operação não foi encontrado.");
        }
    }

    private Optional<UserCompany> resolveUserCompany() {
        return currentUser().flatMap(user -> userCompanyRepository.findByUserId(user.getId()));
    }

    private Optional<User> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            return Optional.empty();
        }
        UUID authId = UUID.fromString(jwtAuth.getToken().getSubject());
        return userRepository.findByAuthId(authId);
    }

    private boolean isAdmin(Position position) {
        return Position.ADMIN_NAME.equals(position.getName());
    }

    private boolean positionHasPermission(UUID positionId, String permissionName) {
        return positionPermissionRepository.existsByPositionIdAndPermission_PermissionName(positionId, permissionName);
    }
}
