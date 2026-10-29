package com.solaria.persistence.service.recommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.solaria.persistence.domain.entity.catalog.Brand;
import com.solaria.persistence.domain.entity.catalog.Model;
import com.solaria.persistence.domain.entity.catalog.Offer;
import com.solaria.persistence.domain.entity.catalog.Supplier;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.identity.Person;
import com.solaria.persistence.domain.entity.professional.Profession;
import com.solaria.persistence.domain.entity.professional.ProfessionalRegistration;
import com.solaria.persistence.domain.entity.professional.Technician;
import com.solaria.persistence.domain.enums.professional.TechnicianStatus;
import com.solaria.persistence.integration.recommendation.RecommendationClientProperties;
import com.solaria.persistence.repository.catalog.OfferRepository;
import com.solaria.persistence.repository.catalog.OfferRepository.SupplierOfferCount;
import com.solaria.persistence.repository.catalog.SupplierRepository;
import com.solaria.persistence.repository.professional.ProfessionalRegistrationRepository;
import com.solaria.persistence.repository.professional.ProfessionalReviewRepository;
import com.solaria.persistence.repository.professional.ProfessionalReviewRepository.ReviewSummary;
import com.solaria.persistence.repository.professional.TechnicianRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class RecommendationFallbackServiceTest {

    private static final UUID COMPANY = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");
    private static final UUID SUPPLIER = UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb");
    private static final UUID SUPPLIER_COMPANY = UUID.fromString("cccccccc-cccc-4ccc-8ccc-cccccccccccc");
    private static final UUID PROFESSION = UUID.fromString("dddddddd-dddd-4ddd-8ddd-dddddddddddd");
    private static final UUID OTHER_PROFESSION = UUID.fromString("eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee");
    private static final Instant NOW = Instant.parse("2026-10-29T12:00:00Z");

    @Mock
    private OfferRepository offers;
    @Mock
    private SupplierRepository suppliers;
    @Mock
    private TechnicianRepository technicians;
    @Mock
    private ProfessionalRegistrationRepository registrations;
    @Mock
    private ProfessionalReviewRepository reviews;

    private RecommendationFallbackService service;

    @BeforeEach
    void setUp() {
        service = serviceWithLimit(10);
    }

    private RecommendationFallbackService serviceWithLimit(int limit) {
        return new RecommendationFallbackService(offers, suppliers, technicians, registrations, reviews,
                new RecommendationClientProperties("", "", Duration.ofSeconds(5), Duration.ofSeconds(25),
                        Duration.ofSeconds(60), limit),
                JsonMapper.builder().build(), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private Company company(UUID id, String tradeName) {
        Company company = new Company();
        company.setId(id);
        company.setTradeName(tradeName);
        company.setCnpj("12345678000199");
        company.setCorporateName("Razao Social Privada");
        return company;
    }

    private Supplier supplier() {
        Supplier supplier = new Supplier();
        supplier.setId(SUPPLIER);
        supplier.setCompany(company(SUPPLIER_COMPANY, "Sol Forte"));
        supplier.setBusinessType("DISTRIBUTOR");
        return supplier;
    }

    private Offer offer(String price, String powerWp) {
        Brand brand = new Brand();
        brand.setName("SunCell");
        Model model = new Model();
        model.setId(UUID.randomUUID());
        model.setBrandEntry(brand);
        model.setModel("SC-550");
        model.setPowerWp(new BigDecimal(powerWp));
        model.setEfficiency(new BigDecimal("21.5"));
        model.setWidth(new BigDecimal("1.1"));
        model.setLength(new BigDecimal("2.2"));
        model.setWeight(new BigDecimal("27"));
        Offer offer = new Offer();
        offer.setId(UUID.randomUUID());
        offer.setSupplier(supplier());
        offer.setModel(model);
        offer.setUnitPrice(new BigDecimal(price));
        offer.setAvailability(40);
        return offer;
    }

    private Technician technician(String name, String slug) {
        Person person = new Person();
        person.setName(name);
        person.setCpf("12345678901");
        person.setBirthDate(LocalDate.of(1990, 1, 1));
        Technician technician = new Technician();
        technician.setId(UUID.randomUUID());
        technician.setStatus(TechnicianStatus.APPROVED);
        technician.setPerson(person);
        technician.setCrea("SP-123456");
        technician.setSlug(slug);
        return technician;
    }

    private Profession profession(UUID id, String name) {
        Profession profession = new Profession();
        profession.setId(id);
        profession.setName(name);
        return profession;
    }

    private ProfessionalRegistration registration(Technician technician, Profession profession,
            OffsetDateTime expiresAt) {
        ProfessionalRegistration registration = new ProfessionalRegistration();
        registration.setTechnician(technician);
        registration.setProfession(profession);
        registration.setNumber("987654");
        registration.setExpirationDate(expiresAt);
        return registration;
    }

    private ReviewSummary summary(Technician technician, double average, long count) {
        return new ReviewSummary() {
            @Override
            public UUID getTechnicianId() {
                return technician.getId();
            }

            @Override
            public Double getAverageRating() {
                return average;
            }

            @Override
            public long getReviewCount() {
                return count;
            }
        };
    }

    private SupplierOfferCount count(UUID supplierId, long total) {
        return new SupplierOfferCount() {
            @Override
            public UUID getSupplierId() {
                return supplierId;
            }

            @Override
            public long getOfferCount() {
                return total;
            }
        };
    }

    @Test
    void offersAreRankedByCostPerWattInTheFeedFormat() {
        Offer cheap = offer("550.00", "550");
        when(offers.findFeedOffers(eq(NOW.atOffset(ZoneOffset.UTC)), any())).thenReturn(List.of(cheap));

        JsonNode body = service.offers("best_value", COMPANY);

        assertEquals("fallback", body.path("source").asString());
        assertEquals("offers", body.path("context").path("feed").asString());
        assertEquals("best_value", body.path("context").path("strategy").asString());
        assertEquals(COMPANY.toString(), body.path("context").path("company_id").asString());
        assertEquals("2026-10-29T12:00:00Z", body.path("context").path("generated_at").asString());
        assertTrue(body.path("context").path("sync_version").isNull());
        assertEquals(1, body.path("warnings").size());
        JsonNode item = body.path("items").get(0);
        assertEquals(1, item.path("rank").asInt());
        assertEquals(cheap.getId().toString(), item.path("offer_id").asString());
        assertEquals(SUPPLIER.toString(), item.path("supplier_id").asString());
        assertEquals("Sol Forte", item.path("supplier_trade_name").asString());
        assertEquals("SunCell", item.path("brand").asString());
        assertEquals("SC-550", item.path("model").asString());
        assertEquals(40, item.path("effective_availability").asInt());
        assertEquals(1.0, item.path("ranking_value").asDouble());
        assertEquals("BRL_per_Wp", item.path("ranking_unit").asString());
        assertEquals(2.42, item.path("dimension").asDouble(), 0.0001);
        assertTrue(item.path("distance_km").isNull());
    }

    @Test
    void offersNeverLeakPrivateCompanyData() {
        when(offers.findFeedOffers(any(), any())).thenReturn(List.of(offer("550.00", "550")));

        String serialized = service.offers(null, null).toString();

        assertFalse(serialized.contains("12345678000199"));
        assertFalse(serialized.contains("Razao Social Privada"));
        assertFalse(serialized.contains("cnpj"));
    }

    @Test
    void theResultLimitIsAppliedAndBounded() {
        when(offers.findFeedOffers(any(), any())).thenReturn(List.of());
        ArgumentCaptor<Pageable> page = ArgumentCaptor.forClass(Pageable.class);

        service.offers(null, null);
        serviceWithLimit(500).offers(null, null);
        serviceWithLimit(0).offers(null, null);

        verify(offers, org.mockito.Mockito.times(3)).findFeedOffers(any(), page.capture());
        assertEquals(List.of(10, 50, 1), page.getAllValues().stream().map(Pageable::getPageSize).toList());
    }

    @Test
    void suppliersAreRankedByActiveOffersWithPublicFieldsOnly() {
        when(offers.countFeedOffersBySupplier(any(), any())).thenReturn(List.of(count(SUPPLIER, 3)));
        when(suppliers.findByIdIn(any())).thenReturn(List.of(supplier()));

        JsonNode body = service.suppliers("nearest", COMPANY);

        assertEquals("fallback", body.path("source").asString());
        assertEquals("suppliers", body.path("context").path("feed").asString());
        JsonNode item = body.path("items").get(0);
        assertEquals(1, item.path("rank").asInt());
        assertEquals(SUPPLIER.toString(), item.path("supplier_id").asString());
        assertEquals(SUPPLIER_COMPANY.toString(), item.path("company_id").asString());
        assertEquals("Sol Forte", item.path("trade_name").asString());
        assertEquals("DISTRIBUTOR", item.path("business_type").asString());
        assertEquals(3, item.path("offer_count").asInt());
        assertEquals("offers", item.path("ranking_unit").asString());
        assertFalse(body.toString().contains("12345678000199"));
    }

    @Test
    void suppliersWithoutOffersReturnAnEmptyFeed() {
        when(offers.countFeedOffersBySupplier(any(), any())).thenReturn(List.of());
        when(suppliers.findByIdIn(any())).thenReturn(List.of());

        JsonNode body = service.suppliers(null, null);

        assertEquals(0, body.path("items").size());
        assertTrue(body.path("context").path("company_id").isNull());
    }

    @Test
    @SuppressWarnings("unchecked")
    void professionalsAreRankedByRatingAndNeverExposePersonalData() {
        Technician best = technician("Ana Souza", "ana-souza");
        Technician other = technician("Bruno Lima", "bruno-lima");
        Technician expired = technician("Carla Dias", "carla-dias");
        Technician unregistered = technician("Davi Reis", "davi-reis");
        when(technicians.findByStatusAndPersonUserActiveTrue(TechnicianStatus.APPROVED))
                .thenReturn(List.of(other, best, expired, unregistered));
        when(registrations.findByTechnicianIdIn(any())).thenReturn(List.of(
                registration(other, profession(PROFESSION, "Engenheiro"), null),
                registration(best, profession(PROFESSION, "Engenheiro"), null),
                registration(best, profession(OTHER_PROFESSION, "Eletricista"),
                        OffsetDateTime.parse("2027-01-01T00:00:00Z")),
                registration(expired, profession(PROFESSION, "Engenheiro"),
                        OffsetDateTime.parse("2026-01-01T00:00:00Z"))));
        when(reviews.summarizeActive(any(Collection.class)))
                .thenReturn(List.of(summary(best, 4.8, 12), summary(other, 4.1, 30)));

        JsonNode body = service.professionals("top_rated", COMPANY, null);

        assertEquals("professionals", body.path("context").path("feed").asString());
        assertEquals(2, body.path("items").size());
        JsonNode first = body.path("items").get(0);
        assertEquals(best.getId().toString(), first.path("technician_id").asString());
        assertEquals("Ana Souza", first.path("name").asString());
        assertEquals(List.of("Eletricista", "Engenheiro"),
                first.path("professions").valueStream().map(JsonNode::asString).toList());
        assertEquals(4.8, first.path("average_rating_global").asDouble());
        assertEquals(12, first.path("review_count_global").asInt());
        assertEquals("rating", first.path("ranking_unit").asString());
        assertEquals(other.getId().toString(), body.path("items").get(1).path("technician_id").asString());
        String serialized = body.toString();
        assertFalse(serialized.contains("12345678901"));
        assertFalse(serialized.contains("1990"));
        assertFalse(serialized.contains("SP-123456"));
        assertFalse(serialized.contains("987654"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void professionalsRespectTheRequestedProfession() {
        Technician engineer = technician("Ana Souza", "ana-souza");
        Technician electrician = technician("Bruno Lima", "bruno-lima");
        when(technicians.findByStatusAndPersonUserActiveTrue(TechnicianStatus.APPROVED))
                .thenReturn(List.of(engineer, electrician));
        when(registrations.findByTechnicianIdIn(any())).thenReturn(List.of(
                registration(engineer, profession(PROFESSION, "Engenheiro"), null),
                registration(electrician, profession(OTHER_PROFESSION, "Eletricista"), null)));
        when(reviews.summarizeActive(any(Collection.class))).thenReturn(List.of());

        JsonNode body = service.professionals(null, null, OTHER_PROFESSION);

        assertEquals(1, body.path("items").size());
        assertEquals(electrician.getId().toString(), body.path("items").get(0).path("technician_id").asString());
        assertEquals(0, body.path("items").get(0).path("review_count_global").asInt());
    }

    @Test
    void noApprovedTechniciansReturnAnEmptyFeedWithoutFurtherQueries() {
        when(technicians.findByStatusAndPersonUserActiveTrue(TechnicianStatus.APPROVED)).thenReturn(List.of());

        JsonNode body = service.professionals(null, null, null);

        assertEquals(0, body.path("items").size());
        assertEquals("fallback", body.path("source").asString());
    }
}
