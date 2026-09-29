package com.solaria.persistence.service.professional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.solaria.persistence.domain.entity.identity.Person;
import com.solaria.persistence.domain.entity.professional.ProfessionalRegistration;
import com.solaria.persistence.domain.entity.professional.Profession;
import com.solaria.persistence.domain.entity.professional.Technician;
import com.solaria.persistence.dto.response.professional.CatalogTechnicianResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.professional.ProfessionalRegistrationRepository;
import com.solaria.persistence.repository.professional.TechnicianRepository;

@ExtendWith(MockitoExtension.class)
class TechnicianCatalogServiceTest {

    @Mock
    private TechnicianRepository technicianRepository;

    @Mock
    private ProfessionalRegistrationRepository professionalRegistrationRepository;

    private TechnicianCatalogService technicianCatalogService;

    private Technician technician;
    private UUID technicianId;

    @BeforeEach
    void setUp() {
        technicianCatalogService = new TechnicianCatalogService(technicianRepository, professionalRegistrationRepository);

        Person person = new Person();
        person.setName("Fulano da Silva");

        technicianId = UUID.randomUUID();
        technician = new Technician();
        technician.setId(technicianId);
        technician.setPerson(person);
        technician.setCrea("SP-123456");
        technician.setSlug("fulano-da-silva");
    }

    @Test
    void findAllMapsEachTechnicianWithItsProfessions() {
        Profession profession = new Profession();
        profession.setName("Eletricista");
        ProfessionalRegistration registration = new ProfessionalRegistration();
        registration.setProfession(profession);

        when(technicianRepository.findAll()).thenReturn(List.of(technician));
        when(professionalRegistrationRepository.findByTechnicianId(technicianId)).thenReturn(List.of(registration));

        List<CatalogTechnicianResponseDTO> result = technicianCatalogService.findAll();

        assertEquals(1, result.size());
        assertEquals("fulano-da-silva", result.get(0).slug());
        assertEquals("Fulano da Silva", result.get(0).name());
        assertEquals(List.of("Eletricista"), result.get(0).professions());
    }

    @Test
    void findBySlugReturnsEmptyProfessionsWhenNoneRegistered() {
        when(technicianRepository.findBySlug("fulano-da-silva")).thenReturn(Optional.of(technician));
        when(professionalRegistrationRepository.findByTechnicianId(technicianId)).thenReturn(List.of());

        CatalogTechnicianResponseDTO result = technicianCatalogService.findBySlug("fulano-da-silva");

        assertEquals("SP-123456", result.crea());
        assertEquals(List.of(), result.professions());
    }

    @Test
    void findBySlugThrowsWhenTechnicianNotFound() {
        when(technicianRepository.findBySlug("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> technicianCatalogService.findBySlug("unknown"));
    }
}
