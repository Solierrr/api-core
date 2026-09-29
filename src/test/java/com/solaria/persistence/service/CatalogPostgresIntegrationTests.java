package com.solaria.persistence.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.enums.company.CompanyType;
import com.solaria.persistence.dto.request.company.CompanyRequestDTO;
import com.solaria.persistence.service.catalog.OfferCatalogService;
import com.solaria.persistence.service.company.CompanyCatalogService;
import com.solaria.persistence.service.company.CompanyService;
import com.solaria.persistence.service.identity.UserCompanyService;
import com.solaria.persistence.service.identity.UserService;

/** Opt-in: use only a disposable PostgreSQL database loaded with the core seed. */
@EnabledIfSystemProperty(named = "solaria.postgres.url", matches = ".+")
@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.datasource.driver-class-name=org.postgresql.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
    "cloudinary.cloud-name=unused", "cloudinary.api-key=unused", "cloudinary.api-secret=unused",
    "google-translate.api-key=unused", "app.integration.auth.base-url=http://127.0.0.1:1",
    "management.tracing.enabled=false", "management.logging.export.enabled=false",
    "management.otlp.metrics.export.enabled=false"
})
@Transactional
class CatalogPostgresIntegrationTests {
    @Autowired CompanyCatalogService catalog;
    @Autowired OfferCatalogService offers;
    @Autowired CompanyService companies;
    @Autowired UserService users;
    @Autowired UserCompanyService memberships;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", () -> System.getProperty("solaria.postgres.url"));
        properties.add("spring.datasource.username", () -> System.getProperty("solaria.postgres.user", "solaria_check"));
        properties.add("spring.datasource.password", () -> System.getProperty("solaria.postgres.password", ""));
    }

    @AfterEach
    void clearPrincipal() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void catalogAndContactResolveSeededSupplier() {
        assertFalse(catalog.findAll().isEmpty());
        assertFalse(offers.findAll("pt-BR").isEmpty());
        assertNotNull(offers.findBySlug("fornecedor-demo", "painel-st-450w", "pt-BR"));
        assertNotNull(companies.findContactUser(UUID.fromString("77777777-7777-7777-7777-777777777777")));
    }

    @Test
    void newDemandantReceivesOwnAdminMembership() {
        UUID authId = UUID.randomUUID();
        var first = users.findMe(authId);
        assertEquals(first.getId(), users.findMe(authId).getId());
        Jwt token = Jwt.withTokenValue("test").header("alg", "none").subject(authId.toString()).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(token));
        CompanyRequestDTO request = new CompanyRequestDTO();
        request.setType(CompanyType.DEMANDANT);
        request.setCnpj("98765432000199");
        request.setTradeName("Demandante de teste");
        request.setCorporateName("Demandante de teste Ltda");
        var company = companies.save(request);
        assertEquals(company.getId(), memberships.findMe().getCompanyId());
        assertEquals(CompanyType.DEMANDANT, company.getType());
    }
}
