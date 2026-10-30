package com.solaria.persistence.service.execution;

import com.solaria.persistence.repository.execution.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;

import java.time.*;
import java.util.*;

/** Durable retry state belongs to the invitation, independent of the email transport. */
@Service
public class ProjectInvitationMailWorker {
    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
    private final ProjectInvitationRepository invites;
    private final TechnicalProjectRepository projects;
    private final TransactionTemplate transaction;
    private final RestClient email;
    private final String key;
    private final String serviceKey;
    private final String web;

    public ProjectInvitationMailWorker(
            ProjectInvitationRepository invites,
            TechnicalProjectRepository projects,
            PlatformTransactionManager manager,
            @Value("${app.projects.email-url:http://localhost:8030}") String base,
            @Value("${app.projects.email-key:}") String key,
            @Value("${app.projects.email-service-key:}") String serviceKey,
            @Value("${app.projects.web-url:http://localhost:5173}") String web) {
        this.invites = invites;
        this.projects = projects;
        this.transaction = new TransactionTemplate(manager);
        this.key = key;
        this.serviceKey = serviceKey;
        this.web = web;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(8));
        email = RestClient.builder().baseUrl(base).requestFactory(factory).build();
    }

    @Scheduled(fixedDelayString = "${app.projects.email-poll-ms:30000}")
    public void tick() {
        if (key.isBlank()) return;
        var pending =
                transaction.execute(
                        s ->
                                invites
                                        .pendingEmail(OffsetDateTime.now(), PageRequest.of(0, 20))
                                        .stream()
                                        .map(i -> i.getId())
                                        .toList());
        if (pending == null) return;
        for (UUID id : pending) send(id);
    }

    void send(UUID id) {
        transaction.executeWithoutResult(
                s -> {
                    var candidate = invites.findById(id).orElse(null);
                    if (candidate == null) return;
                    projects.lockById(candidate.getProjectId());
                    var i = invites.findById(id).orElseThrow();
                    entityManager.refresh(i);
                    if (!"PENDING".equals(i.getEmailState())
                            || i.getStatus()
                                    != com.solaria.persistence.domain.enums.execution
                                            .ProjectInvitationStatus.PENDING
                            || !i.getExpiresAt().isAfter(OffsetDateTime.now())) return;
                    var p = projects.findById(i.getProjectId()).orElseThrow();
                    if (p.isArchived()
                            || p.getStatus()
                                    == com.solaria.persistence.domain.enums.execution.ServiceStatus
                                            .COMPLETED
                            || p.getStatus()
                                    == com.solaria.persistence.domain.enums.execution.ServiceStatus
                                            .CANCELED) return;
                    try {
                        email.post()
                                .uri("/emails")
                                .header("x-api-key", key)
                                .headers(
                                        headers -> {
                                            if (!serviceKey.isBlank())
                                                headers.set("X-Service-Key", serviceKey);
                                        })
                                .body(
                                        Map.of(
                                                "template",
                                                "project-invitation",
                                                "locale",
                                                i.getLocale(),
                                                "to",
                                                i.getEmail(),
                                                "data",
                                                Map.of(
                                                        "projectName",
                                                        p.getName(),
                                                        "projectRole",
                                                        i.getRole().name(),
                                                        "invitationExpiresAt",
                                                        i.getExpiresAt().toString(),
                                                        "actionUrl",
                                                        web.replaceAll("/$", "")
                                                                + "/"
                                                                + i.getLocale()
                                                                + "/"
                                                                + switch (i.getLocale()) {
                                                                    case "es-ES" -> "proyectos";
                                                                    case "en-US" -> "projects";
                                                                    default -> "projetos";
                                                                }
                                                                + "?invitation="
                                                                + i.getId())))
                                .retrieve()
                                .toBodilessEntity();
                        i.setEmailState("SENT");
                    } catch (Exception failure) {
                        i.setEmailAttempts(i.getEmailAttempts() + 1);
                        i.setNextEmailAttemptAt(
                                OffsetDateTime.now()
                                        .plusMinutes(Math.min(60, 1L << i.getEmailAttempts())));
                        if (i.getEmailAttempts() >= 5) i.setEmailState("FAILED");
                    }
                    invites.save(i);
                });
    }
}
