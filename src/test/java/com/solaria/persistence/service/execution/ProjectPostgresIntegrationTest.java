package com.solaria.persistence.service.execution;

import static org.junit.jupiter.api.Assertions.*;

import com.solaria.persistence.domain.entity.execution.ProjectLocation;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.professional.ProfessionalOnboarding;
import com.solaria.persistence.domain.enums.execution.*;
import com.solaria.persistence.dto.request.execution.*;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.repository.identity.UserRepository;
import com.solaria.persistence.repository.professional.ProfessionalOnboardingRepository;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@EnabledIfSystemProperty(named = "solaria.postgres.url", matches = ".+")
@SpringBootTest(
        properties = {
            "spring.jpa.hibernate.ddl-auto=validate",
            "spring.datasource.driver-class-name=org.postgresql.Driver",
            "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
            "cloudinary.cloud-name=unused",
            "cloudinary.api-key=unused",
            "cloudinary.api-secret=unused",
            "google-translate.api-key=unused",
            "app.integration.auth.base-url=http://127.0.0.1:1",
            "management.tracing.enabled=false",
            "management.logging.export.enabled=false",
            "management.otlp.metrics.export.enabled=false"
        })
@Transactional
class ProjectPostgresIntegrationTest {
    @Autowired ProjectService projects;
    @Autowired ProjectActivityService agenda;
    @Autowired ProjectInvitationService invites;
    @Autowired ProjectPhotoService photos;
    @Autowired UserRepository users;
    @Autowired ProfessionalOnboardingRepository profiles;

    @DynamicPropertySource
    static void db(DynamicPropertyRegistry p) {
        p.add("spring.datasource.url", () -> System.getProperty("solaria.postgres.url"));
        p.add("spring.datasource.username", () -> System.getProperty("solaria.postgres.user", "postgres"));
        p.add("spring.datasource.password", () -> System.getProperty("solaria.postgres.password", "local-project-test"));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void personalProjectPersistsWithPrivateAgendaInvitesAndHistory() {
        var user = new User();
        user.setAuth_id(UUID.randomUUID());
        user.setUsername("project_" + UUID.randomUUID().toString().substring(0, 8));
        user.setActive(true);
        user = users.saveAndFlush(user);
        var profile = new ProfessionalOnboarding();
        profile.setId(UUID.randomUUID());
        profile.setUserId(user.getId());
        profile.setAuthId(user.getAuth_id());
        profile.setApprovedVersion(1);
        profiles.saveAndFlush(profile);
        var jwt =
                Jwt.withTokenValue("test")
                        .header("alg", "none")
                        .subject(user.getAuth_id().toString())
                        .claim("email", "owner@solaria.test")
                        .claim("email_verified", true)
                        .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        var loc = new ProjectLocation();
        loc.setState("SP");
        loc.setCity("São Paulo");
        loc.setStreet("Rua de teste");
        var project =
                projects.create(
                        new ProjectRequest(
                                "Projeto real",
                                "Integração PostgreSQL",
                                null,
                                null,
                                loc,
                                null,
                                null));
        assertEquals(user.getId(), project.ownerUserId());
        assertEquals(ProjectRole.OWNER, project.role());
        var start = OffsetDateTime.now().plusDays(1);
        var activity =
                agenda.save(
                        project.id(),
                        null,
                        new ProjectActivityRequest(
                                "Visita técnica",
                                "Conferência",
                                ProjectActivityType.VISIT,
                                start,
                                start.plusHours(1),
                                true,
                                List.of(user.getId())));
        assertThrows(
                BusinessRuleException.class,
                () -> projects.status(project.id(), ServiceStatus.COMPLETED));
        agenda.status(project.id(), activity.id(), ServiceStatus.IN_PROGRESS);
        agenda.status(project.id(), activity.id(), ServiceStatus.COMPLETED);
        invites.create(
                project.id(),
                new ProjectInvitationRequest(
                        "guest@solaria.test", null, ProjectRole.MEMBER, "Técnico", "pt-BR"));
        assertEquals(1, invites.list(project.id()).size());
        assertTrue(photos.list(project.id()).isEmpty());
        byte[] png =
                java.util.Base64.getDecoder()
                        .decode(
                                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aMf8AAAAASUVORK5CYII=");
        var image =
                photos.upload(
                        project.id(),
                        new org.springframework.mock.web.MockMultipartFile(
                                "file", "proof.png", "image/png", png),
                        false);
        photos.cover(project.id(), image.id());
        assertEquals(png.length, photos.list(project.id()).getFirst().size());
        assertTrue(photos.list(project.id()).getFirst().cover());
        assertArrayEquals(png, photos.read(project.id(), image.id()).getContent());
        assertFalse(projects.timeline(project.id()).isEmpty());
        assertEquals(
                ServiceStatus.COMPLETED,
                projects.status(project.id(), ServiceStatus.COMPLETED).status());
        assertFalse(projects.detail(project.id()).canContribute());
        projects.archive(project.id());
        assertTrue(projects.detail(project.id()).archived());
    }

    @Autowired com.solaria.persistence.repository.identity.PersonRepository persons;
    @Autowired com.solaria.persistence.repository.professional.TechnicianRepository technicians;

    @Autowired
    com.solaria.persistence.repository.professional.TechnicianAffiliationRepository affiliations;

    @Autowired com.solaria.persistence.repository.identity.UserCompanyRepository companies;

    @Test
    void approvedIndependentProfessionalExecutesOwnServiceWithoutEmployment() {
        var user = new User();
        user.setAuth_id(UUID.randomUUID());
        user.setUsername("independent_" + UUID.randomUUID().toString().substring(0, 8));
        user.setActive(true);
        user = users.saveAndFlush(user);
        var person = new com.solaria.persistence.domain.entity.identity.Person();
        person.setUser(user);
        person.setName("Profissional de teste");
        person.setCpf("00000000001");
        person.setBirthDate(java.time.LocalDate.of(2000, 1, 1));
        person = persons.saveAndFlush(person);
        var technician = new com.solaria.persistence.domain.entity.professional.Technician();
        technician.setPerson(person);
        technician.setSlug("test_" + UUID.randomUUID());
        technician.setStatus(
                com.solaria.persistence.domain.enums.professional.TechnicianStatus.APPROVED);
        technician = technicians.saveAndFlush(technician);
        var jwt =
                Jwt.withTokenValue("test")
                        .header("alg", "none")
                        .subject(user.getAuth_id().toString())
                        .claim("email", "independent@solaria.test")
                        .claim("email_verified", true)
                        .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        var project =
                projects.create(
                        new ProjectRequest(
                                "Execução profissional", "", null, null, null, null, null));
        var start = OffsetDateTime.now().plusDays(1);
        var activity =
                agenda.save(
                        project.id(),
                        null,
                        new ProjectActivityRequest(
                                "Revisão técnica",
                                "",
                                ProjectActivityType.SERVICE,
                                start,
                                start.plusHours(1),
                                false,
                                List.of(user.getId())));
        assertNotNull(activity.technicalServiceId());
        var affiliation = affiliations.findByTechnicianId(technician.getId()).getFirst();
        assertNull(affiliation.getCompany());
        assertEquals(
                com.solaria.persistence.domain.enums.professional.TechnicalAffiliationType
                        .INDEPENDENT,
                affiliation.getAffiliationType());
        assertTrue(companies.findByUserId(user.getId()).isEmpty());
        agenda.status(project.id(), activity.id(), ServiceStatus.IN_PROGRESS);
        agenda.status(project.id(), activity.id(), ServiceStatus.COMPLETED);
        assertEquals(
                ServiceStatus.COMPLETED,
                projects.status(project.id(), ServiceStatus.COMPLETED).status());
    }
}
