package com.solaria.persistence.service.execution;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.execution.TechnicalService;
import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import com.solaria.persistence.dto.response.execution.PersonalTechnicalServiceResponseDTO;
import com.solaria.persistence.repository.execution.TechnicalServiceRepository;
import com.solaria.persistence.security.mobile.TechnicalServiceMobileAccess;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PersonalTechnicalServiceListService {

    private static final List<ServiceStatus> HISTORICAL_STATUSES = List.of(
            ServiceStatus.COMPLETED,
            ServiceStatus.CANCELED);

    private final TechnicalServiceRepository technicalServiceRepository;
    private final TechnicalServiceMobileAccess technicalServiceMobileAccess;
    private final PersonalTechnicalServiceAssembler personalTechnicalServiceAssembler;

    @Transactional(readOnly = true)
    public List<PersonalTechnicalServiceResponseDTO> findForUser(UUID userId) {
        if (userId == null) {
            return List.of();
        }

        List<TechnicalService> services = technicalServiceRepository
                .findAssignedToUserInMobileScope(userId, HISTORICAL_STATUSES);
        if (services.isEmpty()) {
            return List.of();
        }

        var crew = personalTechnicalServiceAssembler.loadCrew(services);
        return personalTechnicalServiceAssembler.assembleAll(
                services,
                technicalServiceMobileAccess.evaluateAll(services, userId, crew), crew);
    }
}
