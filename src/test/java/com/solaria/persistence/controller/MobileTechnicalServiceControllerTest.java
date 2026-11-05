package com.solaria.persistence.controller;

import com.solaria.persistence.controller.execution.MobileTechnicalServiceController;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.service.execution.MobileTechnicalServiceDirectory;
import com.solaria.persistence.service.execution.ProjectAccess;
import com.solaria.persistence.service.execution.TechnicalServiceService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MobileTechnicalServiceControllerTest {
    @Test void acceptanceActorComesFromAuthenticatedServerContext() {
        var directory = mock(MobileTechnicalServiceDirectory.class);
        var services = mock(TechnicalServiceService.class);
        var access = mock(ProjectAccess.class);
        var actor = new User(); actor.setId(UUID.randomUUID());
        when(access.user()).thenReturn(actor);
        var id = UUID.randomUUID();
        new MobileTechnicalServiceController(directory, services, access).accept(id);
        verify(services).accept(id, actor.getId());
        verify(directory).find(id);
    }
}
