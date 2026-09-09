package com.solaria.persistence.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.repository.identity.UserRepository;

/** Fatos de autorização reutilizados somente pela thread da request MVC atual. */
public final class CurrentUserRequestContext {

    private static final String ATTRIBUTE = CurrentUserRequestContext.class.getName();

    private final UUID authId;
    private final Thread requestThread = Thread.currentThread();
    private Optional<UUID> userId;
    private Optional<User> user;
    private Optional<CompanyAccess> companyAccess;
    private final Map<String, Boolean> endpointGrants = new HashMap<>();

    private CurrentUserRequestContext(UUID authId) {
        this.authId = authId;
    }

    static Optional<CurrentUserRequestContext> forAuthId(UUID authId) {
        return requestAttributes().flatMap(attributes -> {
            CurrentUserRequestContext context = (CurrentUserRequestContext)
                    attributes.getRequest().getAttribute(ATTRIBUTE);
            if (context != null && context.requestThread != Thread.currentThread()) {
                return Optional.empty();
            }
            if (context == null || !context.authId.equals(authId)) {
                context = new CurrentUserRequestContext(authId);
                attributes.getRequest().setAttribute(ATTRIBUTE, context);
            }
            return Optional.of(context);
        });
    }

    public static Optional<CurrentUserRequestContext> current() {
        return requestAttributes()
                .map(attributes -> (CurrentUserRequestContext) attributes.getRequest().getAttribute(ATTRIBUTE))
                .filter(context -> context.requestThread == Thread.currentThread());
    }

    private static Optional<ServletRequestAttributes> requestAttributes() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes
                && !attributes.getRequest().isAsyncStarted()) {
            return Optional.of(attributes);
        }
        return Optional.empty();
    }

    Optional<User> findUser(UserRepository repository) {
        if (user == null) {
            user = userId == null ? repository.findByAuthId(authId) : userId.flatMap(repository::findById);
            userId = user.map(User::getId);
        }
        return user;
    }

    Optional<UUID> findUserId(UserRepository repository) {
        if (userId == null) {
            userId = repository.findIdByAuthId(authId);
        }
        return userId;
    }

    public Optional<CompanyAccess> findCompanyAccess(Supplier<Optional<CompanyAccess>> lookup) {
        if (companyAccess == null) {
            companyAccess = lookup.get();
        }
        return companyAccess;
    }

    public boolean hasEndpointGrant(String endpoint, BooleanSupplier lookup) {
        return endpointGrants.computeIfAbsent(endpoint, ignored -> lookup.getAsBoolean());
    }

    public record CompanyAccess(UUID companyId, UUID positionId, String positionName) {
    }
}
