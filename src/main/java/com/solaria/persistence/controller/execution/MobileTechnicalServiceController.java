package com.solaria.persistence.controller.execution;

import com.solaria.persistence.dto.patch.execution.ServiceScheduleDTO;
import com.solaria.persistence.dto.response.execution.MobileTechnicalServiceResponse;
import com.solaria.persistence.service.execution.MobileTechnicalServiceDirectory;
import com.solaria.persistence.service.execution.ProjectAccess;
import com.solaria.persistence.service.execution.TechnicalServiceService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/services")
@RequiredArgsConstructor
public class MobileTechnicalServiceController {
    private final MobileTechnicalServiceDirectory directory;
    private final TechnicalServiceService services;
    private final ProjectAccess access;

    @GetMapping
    public List<MobileTechnicalServiceResponse> list() { return directory.list(); }

    @GetMapping("/{id}")
    public MobileTechnicalServiceResponse find(@PathVariable UUID id) { return directory.find(id); }

    @PostMapping("/{id}/acceptance")
    public MobileTechnicalServiceResponse accept(@PathVariable UUID id) {
        services.accept(id, access.user().getId());
        return directory.find(id);
    }

    @PostMapping("/{id}/completion")
    public MobileTechnicalServiceResponse complete(@PathVariable UUID id) {
        services.complete(id);
        return directory.find(id);
    }

    @PostMapping("/{id}/cancellation")
    public MobileTechnicalServiceResponse cancel(@PathVariable UUID id) {
        services.cancel(id);
        return directory.find(id);
    }

    @PatchMapping("/{id}/schedule")
    public MobileTechnicalServiceResponse reschedule(@PathVariable UUID id, @Valid @RequestBody ServiceScheduleDTO request) {
        services.reschedule(id, request.getScheduledDate());
        return directory.find(id);
    }
}
