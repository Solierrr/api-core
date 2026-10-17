package com.solaria.persistence.security.mobile;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.execution.TechnicalService;
import com.solaria.persistence.domain.entity.execution.ServiceExecutor;
import com.solaria.persistence.domain.entity.identity.Position;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.execution.MobileServiceAccessRepository;

@Service
@Transactional(readOnly = true)
public class TechnicalServiceMobileAccess {

    public static final String ACCEPT_PERMISSION = "POST /api/technical-services/{id}/acceptance";
    public static final String COMPLETE_PERMISSION = "POST /api/technical-services/{id}/completion";

    public enum ActionMode { REQUESTER, EXECUTOR }
    public enum ServiceAction { ACCEPT, COMPLETE }

    private static final ActorServiceAccess NO_ACCESS = new ActorServiceAccess(false, false, false, false, false);

    private final MobileServiceAccessRepository repository;

    public TechnicalServiceMobileAccess(MobileServiceAccessRepository repository) {
        this.repository = repository;
    }

    public Map<UUID, ActorServiceAccess> evaluateAll(Collection<TechnicalService> services, UUID userId,
            Collection<ServiceExecutor> crew) {
        Map<UUID, ActorServiceAccess> result = new LinkedHashMap<>();
        for (TechnicalService service : services) {
            if (service != null && service.getId() != null) {
                result.put(service.getId(), NO_ACCESS);
            }
        }
        if (result.isEmpty() || userId == null) {
            return Collections.unmodifiableMap(result);
        }

        for (ServiceExecutor executor : crew) {
            var affiliation = executor.getTechnicianAffiliation();
            var actor = affiliation.getTechnician().getPerson().getUser();
            if (actor != null && userId.equals(actor.getId())) {
                mergeExecutor(result, executor.getService().getId(), Boolean.TRUE.equals(affiliation.getActive()));
            }
        }
        for (var row : repository.findRequesterAccess(result.keySet(), userId, ACCEPT_PERMISSION, COMPLETE_PERMISSION)) {
            ActorServiceAccess previous = result.get(row.getServiceId());
            if (previous != null) {
                boolean admin = Position.ADMIN_NAME.equals(row.getPositionName());
                result.put(row.getServiceId(), new ActorServiceAccess(previous.executorRead(),
                        previous.executorActive(), true,
                        previous.requesterCanAccept() || admin || row.getCanAccept(),
                        previous.requesterCanComplete() || admin || row.getCanComplete()));
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private static void mergeExecutor(Map<UUID, ActorServiceAccess> result, UUID serviceId, boolean active) {
        ActorServiceAccess previous = result.get(serviceId);
        if (previous != null) {
            result.put(serviceId, new ActorServiceAccess(true, previous.executorActive() || active,
                    false, false, false));
        }
    }

    public ActorServiceAccess evaluateOne(TechnicalService service, UUID userId, Collection<ServiceExecutor> crew) {
        if (service == null || service.getId() == null) {
            return NO_ACCESS;
        }
        return evaluateAll(Collections.singletonList(service), userId, crew).get(service.getId());
    }

    public ActionMode authorizeAction(ActorServiceAccess access, ServiceAction action) {
        boolean requesterAuthorized = switch (action) {
            case ACCEPT -> access.requesterCanAccept();
            case COMPLETE -> access.requesterCanComplete();
        };
        if (requesterAuthorized) {
            return ActionMode.REQUESTER;
        }
        if (access.executorActive()) {
            return ActionMode.EXECUTOR;
        }
        if (access.requesterMember() || access.executorRead()) {
            throw new UnauthorizedAccessException("Usuário sem autorização para executar esta ação.");
        }
        throw new ResourceNotFoundException("Serviço técnico não encontrado.");
    }
}
