package com.solaria.persistence.service.catalog;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import com.solaria.persistence.domain.entity.catalog.OfferTranslationJob;
import com.solaria.persistence.repository.catalog.OfferTranslationJobRepository;

@Service
@EnableScheduling
public class OfferTranslationQueue {
    private final OfferTranslationJobRepository jobs;
    private final OfferTranslationService translator;
    private final TransactionTemplate transaction;
    private final int maxAttempts;
    private final int leaseSeconds;

    public OfferTranslationQueue(OfferTranslationJobRepository jobs, OfferTranslationService translator,
            PlatformTransactionManager manager,
            @Value("${app.offer-translation.max-attempts:5}") int maxAttempts,
            @Value("${app.offer-translation.lease-seconds:300}") int leaseSeconds) {
        this.jobs = jobs;
        this.translator = translator;
        this.transaction = new TransactionTemplate(manager);
        this.maxAttempts = Math.max(1, maxAttempts);
        this.leaseSeconds = Math.max(60, leaseSeconds);
    }

    @Transactional
    public void enqueue(UUID offerId, UUID sourceId) {
        jobs.cancelOutstanding(offerId);
        var job = new OfferTranslationJob();
        job.setOfferId(offerId);
        job.setSourceTranslationId(sourceId);
        jobs.save(job);
    }

    @Scheduled(fixedDelayString = "${app.offer-translation.poll-ms:5000}")
    public void poll() {
        for (int count = 0; count < 5; count++) {
            var work = transaction.execute(status -> {
                var job = jobs.claimAvailable().orElse(null);
                if (job == null) return null;
                if (job.getAttempts() >= maxAttempts) job.setErrorCode("LEASE_EXHAUSTED");
                job.setStatus("PROCESSING");
                if (job.getAttempts() < maxAttempts) job.setAttempts(job.getAttempts() + 1);
                job.setClaimToken(UUID.randomUUID());
                job.setAvailableAt(Instant.now().plusSeconds(leaseSeconds));
                return jobs.saveAndFlush(job);
            });
            if (work == null) return;
            String outcome;
            boolean terminalizing = work.getAttempts() >= maxAttempts && "LEASE_EXHAUSTED".equals(work.getErrorCode());
            try {
                outcome = terminalizing
                    ? translator.failVersion(work.getOfferId(), work.getSourceTranslationId())
                    : translator.translateVersion(work.getOfferId(), work.getSourceTranslationId());
            } catch (RuntimeException exception) {
                outcome = terminalizing ? "TERMINALIZATION_RETRY" : "FAILED";
            }
            if ("FAILED".equals(outcome) && !terminalizing && work.getAttempts() >= maxAttempts) {
                try {
                    outcome = translator.failVersion(work.getOfferId(), work.getSourceTranslationId());
                } catch (RuntimeException exception) {
                    outcome = "TERMINALIZATION_RETRY";
                }
            }
            String result = outcome;
            transaction.executeWithoutResult(status -> {
                var current = jobs.lockById(work.getId()).orElse(null);
                if (current == null || !"PROCESSING".equals(current.getStatus())
                        || !Objects.equals(current.getClaimToken(), work.getClaimToken())) return;
                if ("TERMINALIZATION_RETRY".equals(result)) {
                    current.setErrorCode("LEASE_EXHAUSTED");
                    current.setStatus("RETRY_PENDING");
                    current.setAvailableAt(Instant.now().plusSeconds(30));
                } else if ("FAILED".equals(result)) {
                    current.setErrorCode("TRANSLATION_UNAVAILABLE");
                    current.setStatus(current.getAttempts() >= maxAttempts ? "FAILED" : "RETRY_PENDING");
                    current.setAvailableAt(Instant.now().plusSeconds(Math.min(3600, 30L << Math.min(7, current.getAttempts() - 1))));
                } else {
                    current.setStatus(result);
                    current.setErrorCode(null);
                }
                current.setClaimToken(null);
                jobs.save(current);
            });
        }
    }
}
