package com.solaria.persistence.controller.catalog;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.catalog.Brand;
import com.solaria.persistence.domain.entity.catalog.Model;
import com.solaria.persistence.domain.entity.catalog.Offer;
import com.solaria.persistence.domain.entity.catalog.Supplier;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.identity.Person;
import com.solaria.persistence.domain.entity.professional.Profession;
import com.solaria.persistence.domain.entity.professional.ProfessionalRegistration;
import com.solaria.persistence.domain.entity.professional.Technician;
import com.solaria.persistence.domain.entity.professional.TechnicianAffiliation;
import com.solaria.persistence.domain.entity.shared.Address;
import com.solaria.persistence.domain.enums.catalog.BrandVisibility;
import com.solaria.persistence.domain.enums.catalog.InfrastructureType;
import com.solaria.persistence.domain.enums.catalog.ModelStatus;
import com.solaria.persistence.domain.enums.catalog.OfferStatus;
import com.solaria.persistence.domain.enums.catalog.PanelType;
import com.solaria.persistence.domain.enums.catalog.ProductType;
import com.solaria.persistence.domain.enums.company.CompanyStatus;
import com.solaria.persistence.domain.enums.company.CompanyType;
import com.solaria.persistence.domain.enums.professional.TechnicalAffiliationType;
import com.solaria.persistence.domain.enums.professional.TechnicianStatus;

import jakarta.persistence.EntityManager;

@SpringBootTest(properties = {
        "app.service-keys.mcp-database=" + InternalCatalogControllerTest.MCP_KEY,
        "app.service-keys.api-auth=" + InternalCatalogControllerTest.AUTH_KEY
})
@AutoConfigureMockMvc
@Transactional
class InternalCatalogControllerTest {

    static final String MCP_KEY = "mcp-database-test-key-0123456789abcdef";
    static final String AUTH_KEY = "api-auth-test-key-0123456789abcdefghij";
    private static final String HEADER = "X-Service-Key";

    @Autowired
    MockMvc mvc;

    @Autowired
    EntityManager em;

    private Company supplierCompany;

    @BeforeEach
    void seed() {
        supplierCompany = company("Solar Sul", "solar-sul", CompanyType.SUPPLIER, "Campinas");
        Supplier supplier = new Supplier();
        supplier.setCompany(supplierCompany);
        em.persist(supplier);

        Brand k2 = brand("K2");
        Brand other = brand("Romagnole");
        offer(supplier, model(k2, "Trilho 4,2 m", InfrastructureType.RAIL), "89.90", 120, "trilho");
        offer(supplier, model(k2, "Grampo final", InfrastructureType.CLAMP), "12.50", 300, "grampo");
        offer(supplier, model(other, "Gancho", InfrastructureType.HOOK), "30.00", 50, "gancho");
        offer(supplier, model(k2, "Kit sem estoque", InfrastructureType.KIT), "5.00", 0, "kit");
        Offer paused = offer(supplier, model(k2, "Suporte pausado", InfrastructureType.BRACKET), "1.00", 10, "suporte");
        paused.setCommercialStatus(OfferStatus.PAUSED);

        Profession electrician = profession("Engenheiro eletricista");
        profession("Ajudante");
        Company installer = company("Instala Mais", "instala-mais", CompanyType.REQUESTER, "Campinas");
        Company remote = company("Norte Solar", "norte-solar", CompanyType.REQUESTER, "Manaus");
        Technician ana = technician("Ana Souza", "ana-souza", "11111111111");
        Technician bruno = technician("Bruno Lima", "bruno-lima", "22222222222");
        affiliation(installer, ana, TechnicalAffiliationType.AFFILIATED);
        affiliation(remote, bruno, TechnicalAffiliationType.PARTNER);
        ProfessionalRegistration registration = new ProfessionalRegistration();
        registration.setTechnician(ana);
        registration.setProfession(electrician);
        registration.setCouncil("CREA-SP");
        registration.setNumber("123456");
        registration.setExpirationDate(OffsetDateTime.of(2027, 3, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        em.persist(registration);
        em.flush();
    }

    @Test
    void professionsRequireTheMcpServiceKey() throws Exception {
        mvc.perform(get("/internal/catalog/professions")).andExpect(status().isUnauthorized());
        mvc.perform(get("/internal/catalog/professions").header(HEADER, "wrong-key-0123456789abcdefghijklmnop"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/internal/catalog/professions").header(HEADER, AUTH_KEY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mcpServiceKeyIsRestrictedToCatalogReads() throws Exception {
        mvc.perform(post("/internal/users").header(HEADER, MCP_KEY))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/internal/catalog/professions").header(HEADER, MCP_KEY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listsProfessionsOrderedByName() throws Exception {
        mvc.perform(get("/internal/catalog/professions").header(HEADER, MCP_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Ajudante"))
                .andExpect(jsonPath("$[1].name").value("Engenheiro eletricista"))
                .andExpect(jsonPath("$[0].id").isNotEmpty());
    }

    @Test
    void listsVisibleInfrastructureOffersByPrice() throws Exception {
        mvc.perform(get("/internal/catalog/infrastructure-offers").header(HEADER, MCP_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].model").value("Grampo final"))
                .andExpect(jsonPath("$[0].unitPrice").value("12.50"))
                .andExpect(jsonPath("$[1].model").value("Gancho"))
                .andExpect(jsonPath("$[2].model").value("Trilho 4,2 m"))
                .andExpect(jsonPath("$[2].supplierTradeName").value("Solar Sul"))
                .andExpect(jsonPath("$[2].brand").value("K2"))
                .andExpect(jsonPath("$[2].infrastructureType").value("RAIL"))
                .andExpect(jsonPath("$[2].availability").value(120))
                .andExpect(jsonPath("$[2].offerId").isNotEmpty());
    }

    @Test
    void filtersInfrastructureOffersByTypeBrandAndLimit() throws Exception {
        mvc.perform(get("/internal/catalog/infrastructure-offers").header(HEADER, MCP_KEY)
                        .param("infrastructureType", "RAIL"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].model").value("Trilho 4,2 m"));
        mvc.perform(get("/internal/catalog/infrastructure-offers").header(HEADER, MCP_KEY).param("brand", "k2"))
                .andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/internal/catalog/infrastructure-offers").header(HEADER, MCP_KEY).param("limit", "1"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].model").value("Grampo final"));
        mvc.perform(get("/internal/catalog/infrastructure-offers").header(HEADER, MCP_KEY).param("limit", "21"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listsAccreditedTechnicians() throws Exception {
        mvc.perform(get("/internal/catalog/accredited-technicians").header(HEADER, MCP_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].technicianName").value("Ana Souza"))
                .andExpect(jsonPath("$[0].affiliatedCompany").value("Instala Mais"))
                .andExpect(jsonPath("$[0].affiliationType").value("AFFILIATED"))
                .andExpect(jsonPath("$[0].profession").value("Engenheiro eletricista"))
                .andExpect(jsonPath("$[0].council").value("CREA-SP"))
                .andExpect(jsonPath("$[0].registrationNumber").value("123456"))
                .andExpect(jsonPath("$[0].registrationExpirationDate").isNotEmpty())
                .andExpect(jsonPath("$[0].city").value("Campinas"))
                .andExpect(jsonPath("$[1].technicianName").value("Bruno Lima"))
                .andExpect(jsonPath("$[1].profession").doesNotExist());
    }

    @Test
    void filtersAccreditedTechniciansByProfessionCityAndLimit() throws Exception {
        mvc.perform(get("/internal/catalog/accredited-technicians").header(HEADER, MCP_KEY)
                        .param("profession", "ELETRICISTA"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].technicianName").value("Ana Souza"));
        mvc.perform(get("/internal/catalog/accredited-technicians").header(HEADER, MCP_KEY).param("city", "manaus"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].technicianName").value("Bruno Lima"));
        mvc.perform(get("/internal/catalog/accredited-technicians").header(HEADER, MCP_KEY).param("limit", "1"))
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/internal/catalog/accredited-technicians").header(HEADER, MCP_KEY).param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    private Company company(String tradeName, String slug, CompanyType type, String city) {
        Company company = new Company();
        company.setType(type);
        company.setStatus(CompanyStatus.APPROVED);
        company.setCnpj(String.valueOf(Math.abs(slug.hashCode())));
        company.setTradeName(tradeName);
        company.setCorporateName(tradeName + " Ltda");
        company.setSlug(slug);
        em.persist(company);
        Address address = new Address();
        address.setOwnerCompany(company);
        address.setState("SP");
        address.setCity(city);
        address.setZipCode("13000000");
        address.setStreet("Rua A");
        em.persist(address);
        company.setAddress(address);
        return company;
    }

    private Brand brand(String name) {
        Brand brand = new Brand();
        brand.setName(name);
        brand.setNormalizedName(name.toLowerCase());
        brand.setVisibility(BrandVisibility.PUBLIC);
        em.persist(brand);
        return brand;
    }

    private Model model(Brand brand, String name, InfrastructureType type) {
        Model model = new Model();
        model.setBrandEntry(brand);
        model.setModel(name);
        model.setProductType(ProductType.INFRASTRUCTURE);
        model.setInfrastructureType(type);
        model.setStatus(ModelStatus.APPROVED);
        model.setCreatorCompanyId(supplierCompany.getId());
        model.setType(PanelType.MONOCRYSTALLINE);
        model.setPowerWp(BigDecimal.ONE);
        model.setEfficiency(BigDecimal.ONE);
        model.setWidth(BigDecimal.ONE);
        model.setLength(BigDecimal.ONE);
        model.setWeight(BigDecimal.ONE);
        em.persist(model);
        return model;
    }

    private Offer offer(Supplier supplier, Model model, String price, int availability, String slug) {
        Offer offer = new Offer();
        offer.setSupplier(supplier);
        offer.setModel(model);
        offer.setUnitPrice(new BigDecimal(price));
        offer.setAvailability(availability);
        offer.setSlug(slug);
        em.persist(offer);
        return offer;
    }

    private Profession profession(String name) {
        Profession profession = new Profession();
        profession.setName(name);
        profession.setAccept_emergency_call(false);
        em.persist(profession);
        return profession;
    }

    private Technician technician(String name, String slug, String cpf) {
        Person person = new Person();
        person.setName(name);
        person.setCpf(cpf);
        person.setBirthDate(LocalDate.of(1990, 1, 1));
        em.persist(person);
        Technician technician = new Technician();
        technician.setPerson(person);
        technician.setCrea("CREA");
        technician.setSlug(slug);
        technician.setStatus(TechnicianStatus.APPROVED);
        em.persist(technician);
        return technician;
    }

    private void affiliation(Company company, Technician technician, TechnicalAffiliationType type) {
        TechnicianAffiliation affiliation = new TechnicianAffiliation();
        affiliation.setCompany(company);
        affiliation.setTechnician(technician);
        affiliation.setAffiliationType(type);
        em.persist(affiliation);
    }
}
