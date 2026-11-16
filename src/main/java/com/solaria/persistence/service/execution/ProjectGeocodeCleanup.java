package com.solaria.persistence.service.execution;

import com.solaria.persistence.repository.execution.TechnicalProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProjectGeocodeCleanup {
    private final TechnicalProjectRepository projects;

    @Scheduled(cron = "0 0 * * * *", zone = "UTC")
    @Transactional
    public void removeExpiredCoordinates() { projects.removeExpiredGeocodes(); }
}
