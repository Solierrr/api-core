package com.solaria.persistence.repository.company;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.catalog.Supplier;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.company.CompanyPlans;
import com.solaria.persistence.domain.entity.company.Subscription;
import com.solaria.persistence.domain.enums.company.CompanyStatus;
import com.solaria.persistence.domain.enums.company.CompanyType;
import com.solaria.persistence.domain.enums.company.PaymentMethod;
import com.solaria.persistence.domain.enums.company.PlanCycle;

import jakarta.persistence.EntityManager;

@EnabledIfSystemProperty(named = "solaria.postgres.url", matches = ".+")
@SpringBootTest(
        properties = {
            "spring.flyway.enabled=true",
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
class SubscriptionChargePostgresIntegrationTest {
    @Autowired SubscriptionRepository subscriptions;
    @Autowired ChargeRepository charges;
    @Autowired EntityManager em;

    @DynamicPropertySource
    static void db(DynamicPropertyRegistry p) {
        p.add("spring.datasource.url", () -> System.getProperty("solaria.postgres.url"));
        p.add("spring.datasource.username", () -> System.getProperty("solaria.postgres.user", "postgres"));
        p.add("spring.datasource.password", () -> System.getProperty("solaria.postgres.password", "local-project-test"));
    }

    @Test
    void generatingTwiceForTheSameDueDateKeepsOneCharge() {
        Subscription subscription = subscription(new BigDecimal("249.90"), "plano-pago");
        LocalDate dueDate = LocalDate.of(2026, 11, 10);

        subscriptions.callGenerateSubscriptionCharge(subscription.getId(), dueDate, PaymentMethod.PIX.name());
        subscriptions.callGenerateSubscriptionCharge(subscription.getId(), dueDate, PaymentMethod.PIX.name());
        em.clear();

        var generated = charges.findBySubscriptionId(subscription.getId());
        assertThat(generated).hasSize(1);
        assertThat(generated.get(0).getAmount()).isEqualByComparingTo("249.90");
        assertThat(generated.get(0).getDueDate()).isEqualTo(dueDate);
    }

    @Test
    void rejectsPlansWithoutPositivePrice() {
        Subscription subscription = subscription(BigDecimal.ZERO, "plano-gratuito");

        assertThatThrownBy(() -> subscriptions.callGenerateSubscriptionCharge(
                subscription.getId(), LocalDate.of(2026, 11, 10), PaymentMethod.PIX.name()))
                .isInstanceOf(DataAccessException.class)
                .rootCause()
                .hasMessageContaining("não tem preço positivo");
    }

    private Subscription subscription(BigDecimal price, String slug) {
        Company company = new Company();
        company.setType(CompanyType.SUPPLIER);
        company.setStatus(CompanyStatus.APPROVED);
        company.setCnpj("12345678000190");
        company.setTradeName("Fornecedor " + slug);
        company.setCorporateName("Fornecedor " + slug + " Ltda");
        company.setSlug(slug);
        em.persist(company);
        Supplier supplier = new Supplier();
        supplier.setCompany(company);
        em.persist(supplier);
        CompanyPlans plan = new CompanyPlans();
        plan.setName("Plano " + slug);
        plan.setValue(price);
        plan.setCycle(PlanCycle.MONTHLY);
        em.persist(plan);
        Subscription subscription = new Subscription();
        subscription.setSupplier(supplier);
        subscription.setPlan(plan);
        subscription.setStartDate(OffsetDateTime.now());
        em.persist(subscription);
        em.flush();
        return subscription;
    }
}
