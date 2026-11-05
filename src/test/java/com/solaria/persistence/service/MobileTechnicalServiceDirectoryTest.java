package com.solaria.persistence.service;

import com.solaria.persistence.domain.entity.execution.TechnicalProject;
import com.solaria.persistence.domain.entity.execution.TechnicalService;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.enums.execution.ProjectRole;
import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.execution.ProjectActivityRepository;
import com.solaria.persistence.repository.execution.ServiceExecutorRepository;
import com.solaria.persistence.repository.execution.TechnicalProjectRepository;
import com.solaria.persistence.repository.execution.TechnicalServiceRepository;
import com.solaria.persistence.service.execution.MobileTechnicalServiceDirectory;
import com.solaria.persistence.service.execution.ProjectAccess;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MobileTechnicalServiceDirectoryTest {
    private final ProjectAccess access = mock(ProjectAccess.class);
    private final TechnicalProjectRepository projects = mock(TechnicalProjectRepository.class);
    private final TechnicalServiceRepository services = mock(TechnicalServiceRepository.class);
    private final ServiceExecutorRepository executors = mock(ServiceExecutorRepository.class);
    private final ProjectActivityRepository activities = mock(ProjectActivityRepository.class);
    private final MobileTechnicalServiceDirectory directory = new MobileTechnicalServiceDirectory(access, projects, services, executors, activities);

    private TechnicalProject project() {
        var p = new TechnicalProject();
        p.setId(UUID.randomUUID());
        p.setName("Projeto autorizado");
        p.setStatus(ServiceStatus.OPEN);
        return p;
    }

    private TechnicalService service(TechnicalProject project) {
        var s = new TechnicalService();
        s.setId(UUID.randomUUID());
        s.setPurpose("Revisão do sistema");
        s.setTechnicalProject(project);
        return s;
    }

    @Test void listQueriesOnlyTheProjectsOfTheAuthenticatedActor() {
        var actor = new User(); actor.setId(UUID.randomUUID());
        var project = project();
        var service = service(project);
        when(access.user()).thenReturn(actor);
        when(projects.findAccessible(actor.getId(), null, null)).thenReturn(List.of(project));
        when(services.findByTechnicalProject_IdInOrderByCreatedAtDesc(List.of(project.getId()))).thenReturn(List.of(service));
        when(access.read(project)).thenReturn(ProjectRole.MEMBER);
        var result = directory.list();
        assertEquals(1, result.size());
        assertEquals(project.getId(), result.getFirst().projectId());
        assertFalse(result.getFirst().canManage());
        verify(services, never()).findAll();
    }

    @Test void callerWithoutProjectsGetsAnEmptyListWithoutGlobalServiceQuery() {
        var actor = new User(); actor.setId(UUID.randomUUID());
        when(access.user()).thenReturn(actor);
        when(projects.findAccessible(actor.getId(), null, null)).thenReturn(List.of());
        assertTrue(directory.list().isEmpty());
        verify(services, never()).findAll();
    }

    @Test void managedActorListUsesItsCompanyAsMandatoryQueryScope() {
        var actor = new User(); actor.setId(UUID.randomUUID()); actor.setManagedByCompanyId(UUID.randomUUID());
        when(access.user()).thenReturn(actor);
        when(projects.findAccessible(actor.getId(), actor.getManagedByCompanyId(), null)).thenReturn(List.of());
        assertTrue(directory.list().isEmpty());
        verify(projects).findAccessible(actor.getId(), actor.getManagedByCompanyId(), null);
    }

    @Test void detailNeverExposesServiceFromAnotherProject() {
        var project = project();
        var service = service(project);
        when(services.findById(service.getId())).thenReturn(Optional.of(service));
        when(access.read(project)).thenThrow(new UnauthorizedAccessException("Projeto indisponível"));
        assertThrows(UnauthorizedAccessException.class, () -> directory.find(service.getId()));
    }

    @Test void linkedAgendaAndArchivedProjectAreReadOnlyEvenForOwner() {
        var project = project();
        var service = service(project);
        when(services.findById(service.getId())).thenReturn(Optional.of(service));
        when(access.read(project)).thenReturn(ProjectRole.OWNER);
        when(executors.existsByServiceId(service.getId())).thenReturn(true);
        assertTrue(directory.find(service.getId()).canManage());
        when(activities.existsByTechnicalServiceId(service.getId())).thenReturn(true);
        assertFalse(directory.find(service.getId()).canManage());
        assertTrue(directory.find(service.getId()).managedByAgenda());
        when(activities.existsByTechnicalServiceId(service.getId())).thenReturn(false);
        project.setArchived(true);
        assertFalse(directory.find(service.getId()).canManage());
    }
}
