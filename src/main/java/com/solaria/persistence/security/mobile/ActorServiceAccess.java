package com.solaria.persistence.security.mobile;

import java.util.List;

import com.solaria.persistence.domain.enums.execution.ServiceStatus;

public record ActorServiceAccess(boolean executorRead, boolean executorActive, boolean requesterMember,
        boolean requesterCanAccept, boolean requesterCanComplete) {

    public String myRole() {
        if (executorRead && requesterMember) {
            return "EXECUTOR_AND_REQUESTER";
        }
        if (executorRead) {
            return "EXECUTOR";
        }
        return requesterMember ? "REQUESTER" : null;
    }

    public List<String> allowedActions(ServiceStatus status, boolean hasExecutors) {
        if (status == ServiceStatus.OPEN && hasExecutors && (requesterCanAccept || executorActive)) {
            return List.of("ACCEPT");
        }
        if (status == ServiceStatus.IN_PROGRESS && (requesterCanComplete || executorActive)) {
            return List.of("COMPLETE");
        }
        return List.of();
    }
}
