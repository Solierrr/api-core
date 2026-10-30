package com.solaria.persistence.service.professional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.professional.*;
import com.solaria.persistence.domain.enums.professional.TechnicianStatus;
import com.solaria.persistence.dto.professional.ProfessionalOnboardingContract.*;
import com.solaria.persistence.repository.identity.*;
import com.solaria.persistence.repository.professional.*;
import com.solaria.persistence.repository.shared.ContactRepository;
import com.solaria.persistence.security.CurrentUserService;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

class ProfessionalOnboardingServiceTest {
    private final ProfessionalOnboardingRepository profiles = mock(ProfessionalOnboardingRepository.class);
    private final ProfessionalSubmissionRepository submissions = mock(ProfessionalSubmissionRepository.class);
    private final ProfessionalDocumentRepository documents = mock(ProfessionalDocumentRepository.class);
    private final ProfessionRepository professions = mock(ProfessionRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final TechnicianRepository technicians = mock(TechnicianRepository.class);
    private final PlatformAdminRepository admins = mock(PlatformAdminRepository.class);
    private final CurrentUserService current = mock(CurrentUserService.class);
    private final ObjectMapper json = new ObjectMapper();
    private final ProfessionalDocumentStorageService storage = mock(ProfessionalDocumentStorageService.class);
    private final ProfessionalOnboardingService service = new ProfessionalOnboardingService(profiles, submissions, documents,
        professions, users, mock(PersonRepository.class), technicians, mock(ProfessionalRegistrationRepository.class),
        mock(ContactRepository.class), admins, current, json, storage);

    @Test
    void requirementsDependOnProfessionAndMissingConfigurationCannotAutoapprove() {
        var profession = new Profession();
        profession.setId(UUID.randomUUID());
        profession.setName("Eletricista");
        profession.setRequiresRegistration(false);
        profession.setRequiresNr10(true);
        profession.setRequiresNr35(false);
        when(professions.findById(profession.getId())).thenReturn(Optional.of(profession));
        assertEquals(List.of("IDENTITY", "NR10"), service.policy(profession.getId()).requiredDocuments());
        assertTrue(service.policy(profession.getId()).configured());
        profession.setRequiresNr35(null);
        assertFalse(service.policy(profession.getId()).configured());
    }

    @Test
    void pendingOrRejectedProposalDoesNotChangeApprovedEligibility() {
        var user = new User(); user.setId(UUID.randomUUID()); user.setAuthId(UUID.randomUUID());
        var profile = new ProfessionalOnboarding(); profile.setId(UUID.randomUUID()); profile.setAuthId(user.getAuthId());
        profile.setUserId(user.getId()); profile.setTechnicianId(UUID.randomUUID()); profile.setApprovedVersion(1);
        profile.setApprovedJson("{}"); profile.setDraftJson("{\"name\":\"Proposta\"}");
        var technician = new Technician(); technician.setStatus(TechnicianStatus.APPROVED);
        when(users.findByAuthId(user.getAuthId())).thenReturn(Optional.of(user));
        when(profiles.findByAuthId(user.getAuthId())).thenReturn(Optional.of(profile));
        when(technicians.findById(profile.getTechnicianId())).thenReturn(Optional.of(technician));
        assertTrue(service.eligibility(user.getAuthId()).eligible());
        verifyNoInteractions(submissions);
        profile.setSuspended(true);
        assertFalse(service.eligibility(user.getAuthId()).eligible());
        profile.setSuspended(false); user.setActive(false);
        assertFalse(service.eligibility(user.getAuthId()).eligible());
    }

    @Test
    void rejectedChangeKeepsApprovedSnapshotAndRecordsActorAndReason() {
        var admin = new User(); admin.setId(UUID.randomUUID());
        var profile = new ProfessionalOnboarding(); profile.setId(UUID.randomUUID()); profile.setApprovedVersion(4);
        profile.setApprovedJson("{\"revision\":4,\"name\":\"Nome aprovado\"}");
        var submission = new ProfessionalSubmission(); submission.setId(UUID.randomUUID()); submission.setOnboardingId(profile.getId());
        submission.setRevision(5); submission.setBaseApprovedVersion(4); submission.setStatus("MANUAL_REVIEW");
        submission.setSnapshotJson("{\"revision\":5,\"name\":\"Nome proposto\"}"); submission.setPolicyJson("{}");
        when(current.getCurrentUser()).thenReturn(admin);
        when(admins.existsByUserId(admin.getId())).thenReturn(true);
        when(submissions.lockById(submission.getId())).thenReturn(Optional.of(submission));
        when(profiles.lockById(profile.getId())).thenReturn(Optional.of(profile));
        when(profiles.findById(profile.getId())).thenReturn(Optional.of(profile));
        var decision = service.decide(submission.getId(), new Decision(false, 5, "Identidade divergente"));
        assertEquals("REJECTED", decision.status());
        assertEquals(admin.getId(), decision.decidedBy());
        assertEquals("Identidade divergente", decision.decisionReason());
        assertEquals(4, profile.getApprovedVersion());
        assertEquals("{\"revision\":4,\"name\":\"Nome aprovado\"}", profile.getApprovedJson());
        verifyNoInteractions(technicians);
        assertThrows(ResponseStatusException.class, () -> service.decide(submission.getId(), new Decision(true, 5, "Outra decisão")));
    }

    @Test
    void privateEvidenceCannotBeReadByAnotherUser() {
        var user = new User(); user.setId(UUID.randomUUID());
        var document = new ProfessionalDocument(); document.setId(UUID.randomUUID()); document.setUserId(UUID.randomUUID());
        when(current.getCurrentUser()).thenReturn(user);
        when(documents.findById(document.getId())).thenReturn(Optional.of(document));
        assertThrows(ResponseStatusException.class, () -> service.documentContent(document.getId()));
        verifyNoInteractions(storage);
    }
    @Test
    void uploadsAreStagedUntilValidationConfirmsThem() throws Exception {
        var user = new User(); user.setId(UUID.randomUUID());
        when(current.getCurrentUser()).thenReturn(user);
        when(users.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        var file = new org.springframework.mock.web.MockMultipartFile("file", "certificate.png", "image/png", new byte[]{1, 2, 3});
        service.upload("NR10", file);
        var captured = org.mockito.ArgumentCaptor.forClass(ProfessionalDocument.class);
        verify(documents).saveAndFlush(captured.capture());
        assertArrayEquals(file.getBytes(), captured.getValue().getContent());
        assertNull(captured.getValue().getCloudinaryPublicId());
        verifyNoInteractions(storage);
    }

    @Test void reviewQueueRequiresPlatformAdministratorAndIncludesMoreThanOnePage() {
        var user = new User(); user.setId(UUID.randomUUID());
        when(current.getCurrentUser()).thenReturn(user);
        assertThrows(ResponseStatusException.class, service::reviewQueue);
        verifyNoInteractions(submissions);
        when(admins.existsByUserId(user.getId())).thenReturn(true);
        var rows = java.util.stream.IntStream.range(0, 31).mapToObj(index -> {
            var row = new ProfessionalSubmission(); row.setId(UUID.randomUUID());
            row.setCreatedAt(java.time.Instant.parse("2026-10-11T00:00:00Z").plusSeconds(index));
            row.setSnapshotJson("{\"revision\":1,\"name\":\"Candidate " + index + "\",\"registrationNumber\":\"123\"}");
            return row;
        }).toList();
        when(submissions.findByStatusOrderByCreatedAtAsc(eq("MANUAL_REVIEW"), any())).thenReturn(rows);
        var queue = service.reviewQueue();
        assertEquals(31, queue.size());
        assertEquals("Candidate 0", queue.getFirst().name());
        assertEquals("Candidate 30", queue.getLast().name());
        verify(submissions).findByStatusOrderByCreatedAtAsc("MANUAL_REVIEW", org.springframework.data.domain.Pageable.unpaged());
    }
}
