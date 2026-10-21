package com.solaria.persistence.service.professional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.web.client.RestClient;
import com.solaria.persistence.domain.entity.professional.*;
import com.solaria.persistence.dto.professional.ProfessionalOnboardingContract.*;
import com.solaria.persistence.repository.professional.*;
import com.solaria.persistence.exception.StorageException;
import tools.jackson.databind.ObjectMapper;

class ProfessionalValidationWorkerFlowTest {
    private final ProfessionalSubmissionRepository submissions = mock(ProfessionalSubmissionRepository.class);
    private final ProfessionalOnboardingRepository profiles = mock(ProfessionalOnboardingRepository.class);
    private final ProfessionalDocumentRepository documents = mock(ProfessionalDocumentRepository.class);
    private final ProfessionalOnboardingService onboarding = mock(ProfessionalOnboardingService.class);
    private final ProfessionalDocumentStorageService storage = mock(ProfessionalDocumentStorageService.class);
    private final PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
    private final ObjectMapper json = new ObjectMapper();
    private final ProfessionalSubmission submission = new ProfessionalSubmission();
    private final ProfessionalOnboarding profile = new ProfessionalOnboarding();
    private final ProfessionalDocument document = new ProfessionalDocument();
    private ProfessionalValidationWorker worker;
    private MockRestServiceServer server;
    private Draft draft;
    private Policy policy;
    @BeforeEach void setup() {
        when(manager.getTransaction(any())).thenAnswer(invocation -> new SimpleTransactionStatus());
        profile.setId(UUID.randomUUID()); profile.setUserId(UUID.randomUUID());
        submission.setId(UUID.randomUUID()); submission.setOnboardingId(profile.getId()); submission.setRevision(1);
        submission.setSnapshotJson("snapshot"); submission.setPolicyJson("policy");
        document.setId(UUID.randomUUID()); document.setUserId(profile.getUserId()); document.setKind("IDENTITY");
        document.setSha256("hash"); document.setContentType("image/png");
        draft = new Draft(1, "Nome", "52998224725", LocalDate.of(1990,1,1), "email@example.com", "11999999999", UUID.randomUUID(), null,null,null,List.of(document.getId()));
        policy = new Policy(1,true,false,"Tecnico",List.of("IDENTITY"),List.of("IDENTITY_MATCH"));
        when(submissions.lockById(submission.getId())).thenReturn(Optional.of(submission));
        when(submissions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(profiles.findById(profile.getId())).thenReturn(Optional.of(profile));
        when(profiles.lockById(profile.getId())).thenReturn(Optional.of(profile));
        when(documents.findById(document.getId())).thenReturn(Optional.of(document));
        when(onboarding.readDraft("snapshot")).thenReturn(draft); when(onboarding.readPolicy("policy")).thenReturn(policy);
        when(storage.read(document)).thenReturn(new byte[]{1,2,3});
        worker = new ProfessionalValidationWorker(submissions, profiles, documents, onboarding, storage, json, manager, "http://validation.test", "service-token");
        var builder = RestClient.builder().baseUrl("http://validation.test"); server = MockRestServiceServer.bindTo(builder).build();
        ReflectionTestUtils.setField(worker, "client", builder.build());
    }
    private String result(boolean passed) {
        return json.writeValueAsString(Map.of("submissionId",submission.getId(), "revision",1,"policyVersion",1,
            "status",passed ? "PASSED" : "FAILED", "criteriaResults", List.of(Map.of("code","IDENTITY_MATCH","result",passed ? "PASSED" : "FAILED","documentIds",List.of(document.getId())))));
    }
    @Test void rejectedValidationDoesNotApproveOrArchiveEvidence() {
        server.expect(requestTo("http://validation.test/api/v1/professionals/validate")).andRespond(withSuccess(result(false), MediaType.APPLICATION_JSON));
        worker.process(submission.getId());
        assertEquals("MANUAL_REVIEW", submission.getStatus());
        verify(onboarding, never()).apply(any(), any(), anySet()); verify(storage, never()).archive(any()); server.verify();
    }
    @Test void matchingPositiveValidationApprovesOnlyConfirmedDocuments() {
        server.expect(requestTo("http://validation.test/api/v1/professionals/validate")).andRespond(withSuccess(result(true), MediaType.APPLICATION_JSON));
        worker.process(submission.getId());
        assertEquals("APPROVED",submission.getStatus());
        verify(onboarding).apply(profile, submission, Set.of(document.getId())); server.verify();
    }
    @Test void storageFailureKeepsConfirmedValidationForRetryWithoutAnotherAiCall() {
        String confirmed = result(true); submission.setResultJson(confirmed);
        doThrow(new StorageException("Cloudinary unavailable")).when(onboarding).apply(any(), any(), anySet());
        worker.process(submission.getId());
        assertEquals("RETRY_PENDING",submission.getStatus()); assertEquals(confirmed,submission.getResultJson());
        assertNotNull(submission.getAvailableAt()); server.verify();
    }
}
