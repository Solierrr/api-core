package com.solaria.persistence.service.catalog;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import com.solaria.persistence.domain.entity.catalog.OfferTranslationJob;
import com.solaria.persistence.repository.catalog.OfferTranslationJobRepository;

class OfferTranslationQueueTest {
    private final OfferTranslationJobRepository jobs = mock(OfferTranslationJobRepository.class);
    private final OfferTranslationService translator = mock(OfferTranslationService.class);
    private final PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
    private OfferTranslationQueue queue;
    @BeforeEach void setup() {
        when(manager.getTransaction(any())).thenAnswer(invocation -> new SimpleTransactionStatus());
        when(jobs.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        queue = new OfferTranslationQueue(jobs,translator,manager,5,300);
    }
    private OfferTranslationJob fixture(String outcome) {
        var job = new OfferTranslationJob(); job.setOfferId(UUID.randomUUID()); job.setSourceTranslationId(UUID.randomUUID());
        when(jobs.claimAvailable()).thenReturn(Optional.of(job),Optional.empty());
        when(jobs.lockById(job.getId())).thenReturn(Optional.of(job));
        when(translator.translateVersion(job.getOfferId(),job.getSourceTranslationId())).thenReturn(outcome);
        return job;
    }
    @Test void providerFailureIsRetriedWithBackoff() {
        var job = fixture("FAILED"); queue.poll();
        assertEquals("RETRY_PENDING",job.getStatus()); assertEquals(1,job.getAttempts()); assertNull(job.getClaimToken());
        assertEquals("TRANSLATION_UNAVAILABLE",job.getErrorCode());
        assertTrue(job.getAvailableAt().isAfter(java.time.Instant.now()));
    }
    @Test void abandonedClaimCanBeRecoveredAndCompleted() {
        var job = fixture("COMPLETED"); job.setStatus("PROCESSING"); job.setAttempts(1); job.setClaimToken(UUID.randomUUID());
        queue.poll(); assertEquals("COMPLETED",job.getStatus()); assertEquals(2,job.getAttempts());
    }
    @Test void obsoleteSourceDoesNotKeepRetrying() {
        var job = fixture("CANCELLED"); queue.poll(); assertEquals("CANCELLED",job.getStatus());
    }
    @Test void enqueueCancelsPreviousRevisionAndPersistsNewJob() {
        var offerId=UUID.randomUUID(); var sourceId=UUID.randomUUID(); queue.enqueue(offerId,sourceId);
        verify(jobs).cancelOutstanding(offerId);
        var capture=org.mockito.ArgumentCaptor.forClass(OfferTranslationJob.class); verify(jobs).save(capture.capture());
        assertEquals(sourceId,capture.getValue().getSourceTranslationId()); verifyNoInteractions(translator);
    }
    @Test void exhaustedLeaseMarksCurrentSourceFailedWithoutAnotherProviderCall() {
        var job = fixture("COMPLETED"); job.setAttempts(5); job.setStatus("PROCESSING");
        when(translator.failVersion(job.getOfferId(),job.getSourceTranslationId())).thenReturn("FAILED");
        queue.poll(); assertEquals("FAILED",job.getStatus());
        verify(translator,never()).translateVersion(any(),any());
        verify(translator).failVersion(job.getOfferId(),job.getSourceTranslationId());
    }
    @Test void persistenceFailureWhileTerminalizingRemainsRecoverableWithoutProvider() {
        var job = fixture("COMPLETED"); job.setAttempts(5); job.setStatus("PROCESSING");
        when(translator.failVersion(job.getOfferId(),job.getSourceTranslationId())).thenThrow(new IllegalStateException("database timeout"));
        queue.poll(); assertEquals("RETRY_PENDING",job.getStatus()); assertEquals("LEASE_EXHAUSTED",job.getErrorCode());
        assertEquals(5,job.getAttempts()); verify(translator,never()).translateVersion(any(),any());
    }
}
