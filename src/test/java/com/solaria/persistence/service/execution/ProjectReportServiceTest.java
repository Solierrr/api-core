package com.solaria.persistence.service.execution;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.solaria.persistence.domain.entity.execution.*;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.dto.response.execution.ProjectPhotoResponse;
import com.solaria.persistence.exception.*;
import com.solaria.persistence.repository.execution.*;
import com.solaria.persistence.repository.identity.UserRepository;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class ProjectReportServiceTest {
    @Mock ProjectService projects;
    @Mock ProjectAccess access;
    @Mock ProjectPhotoService photoService;
    @Mock ProjectReportRepository reports;
    @Mock ProjectPhotoRepository photos;
    @Mock UserRepository users;
    ProjectReportService service;
    User user;
    TechnicalProject project;

    @BeforeEach
    void setup() {
        service = new ProjectReportService(projects, access, photoService, reports, photos, users);
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("Ana");
        project = new TechnicalProject();
        project.setId(UUID.randomUUID());
        lenient().when(access.user()).thenReturn(user);
        lenient().when(projects.entity(project.getId(), true)).thenReturn(project);
        lenient().when(projects.entity(project.getId(), false)).thenReturn(project);
        lenient().when(users.findById(user.getId())).thenReturn(Optional.of(user));
        lenient().when(reports.save(any(ProjectReport.class))).thenAnswer(call -> call.getArgument(0));
        lenient().when(photos.ofReport(eq(project.getId()), any())).thenReturn(List.of());
    }

    private MockMultipartFile image(String name) {
        return new MockMultipartFile("files", name, "image/png", new byte[] {1});
    }

    @Test
    void aReportStoresItsImagesAsProjectPhotosAndEntersTheHistory() {
        var response = service.create(project.getId(), "  Visita técnica ", " Telhado conferido. ", List.of(image("a.png"), image("b.png")));

        assertEquals("Visita técnica", response.title());
        assertEquals("Telhado conferido.", response.content());
        assertEquals("Ana", response.author());
        verify(photoService, times(2)).storeForReport(eq(project), any(), eq(response.id()));
        verify(projects).record(project, "REPORT_ADDED", "Visita técnica", response.id());
    }

    @Test
    void aReportWithoutImagesIsValid() {
        var response = service.create(project.getId(), "Reunião", "Alinhamento com o cliente.", null);

        assertEquals("Reunião", response.title());
        verifyNoInteractions(photoService);
    }

    @Test
    void aViewerCannotWriteReports() {
        doThrow(new UnauthorizedAccessException("Acesso somente para consulta")).when(access).contribute(project);

        assertThrows(UnauthorizedAccessException.class, () -> service.create(project.getId(), "Título", "Conteúdo", null));
        verify(reports, never()).save(any());
    }

    @Test
    void titleAndContentAreRequired() {
        assertThrows(BusinessRuleException.class, () -> service.create(project.getId(), "   ", "Conteúdo", null));
        assertThrows(BusinessRuleException.class, () -> service.create(project.getId(), "Título", " ", null));
        assertThrows(BusinessRuleException.class, () -> service.create(project.getId(), "t".repeat(161), "Conteúdo", null));
        assertThrows(BusinessRuleException.class, () -> service.create(project.getId(), "Título", "c".repeat(4001), null));
        verify(reports, never()).save(any());
    }

    @Test
    void aReportAcceptsAtMostFiveImages() {
        var many = new ArrayList<org.springframework.web.multipart.MultipartFile>();
        for (int i = 0; i < ProjectReportService.MAX_IMAGES + 1; i++) many.add(image("f" + i + ".png"));

        assertThrows(BusinessRuleException.class, () -> service.create(project.getId(), "Título", "Conteúdo", many));
        verify(reports, never()).save(any());
    }

    @Test
    void aReportFromAnotherProjectIsNotFound() {
        var report = new ProjectReport();
        report.setProjectId(UUID.randomUUID());
        when(reports.findById(report.getId())).thenReturn(Optional.of(report));

        assertThrows(ResourceNotFoundException.class, () -> service.get(project.getId(), report.getId()));
    }

    @Test
    void theDetailCarriesTheReportImages() {
        var report = new ProjectReport();
        report.setProjectId(project.getId());
        report.setAuthorId(user.getId());
        report.setTitle("Vistoria");
        report.setContent("Tudo certo.");
        var photo = new ProjectPhotoResponse(UUID.randomUUID(), "a.png", "image/png", false, 1, report.getCreatedAt());
        when(reports.findById(report.getId())).thenReturn(Optional.of(report));
        when(photos.ofReport(project.getId(), report.getId())).thenReturn(List.of(photo));

        var response = service.get(project.getId(), report.getId());

        assertEquals(List.of(photo), response.photos());
        assertEquals("Vistoria", response.title());
    }
}
