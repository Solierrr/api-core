package com.solaria.persistence.service.execution;

import com.solaria.persistence.domain.entity.execution.TechnicalService;
import com.solaria.persistence.domain.enums.execution.ProjectRole;
import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import com.solaria.persistence.dto.response.execution.MobileTechnicalServiceResponse;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.execution.ProjectActivityRepository;
import com.solaria.persistence.repository.execution.ServiceExecutorRepository;
import com.solaria.persistence.repository.execution.TechnicalProjectRepository;
import com.solaria.persistence.repository.execution.TechnicalServiceRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MobileTechnicalServiceDirectory {
    private final ProjectAccess access;
    private final TechnicalProjectRepository projects;
    private final TechnicalServiceRepository services;
    private final ServiceExecutorRepository executors;
    private final ProjectActivityRepository activities;

    public List<MobileTechnicalServiceResponse> list() {
        var actor = access.user();
        var ids = projects.findAccessible(actor.getId(), actor.getManagedByCompanyId(), access.adminCompany(actor)).stream()
                .map(project -> project.getId()).toList();
        if (ids.isEmpty()) return List.of();
        return services.findByTechnicalProject_IdInOrderByCreatedAtDesc(ids).stream().map(this::view).toList();
    }

    public MobileTechnicalServiceResponse find(UUID id) {
        return view(services.findById(id).orElseThrow(() -> new ResourceNotFoundException("Serviço técnico indisponível")));
    }

    private MobileTechnicalServiceResponse view(TechnicalService service) {
        var project = service.getTechnicalProject();
        var role = access.read(project);
        boolean linked = activities.existsByTechnicalServiceId(service.getId());
        boolean mutable = !project.isArchived() && project.getStatus() != ServiceStatus.COMPLETED
                && project.getStatus() != ServiceStatus.CANCELED;
        boolean manage = (role == ProjectRole.OWNER || role == ProjectRole.MANAGER) && mutable && !linked;
        return new MobileTechnicalServiceResponse(service.getId(), project.getId(), project.getName(), service.getPurpose(),
                service.getStatus(), service.getScheduledDate(), service.getAcceptedAt(), service.getEndDate(),
                project.getLocation(), manage, executors.existsByServiceId(service.getId()), linked);
    }
}
