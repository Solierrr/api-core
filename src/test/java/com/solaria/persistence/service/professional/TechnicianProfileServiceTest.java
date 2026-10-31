package com.solaria.persistence.service.professional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.professional.TechnicianProfile;
import com.solaria.persistence.domain.enums.professional.TechnicianSpecialty;
import com.solaria.persistence.dto.request.professional.TechnicianProfileRequest;
import com.solaria.persistence.dto.response.professional.TechnicianProfileResponse;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.identity.SelfProfileRepository;
import com.solaria.persistence.repository.professional.TechnicianProfileRepository;
import com.solaria.persistence.security.CurrentUserService;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class TechnicianProfileServiceTest {
    @Mock CurrentUserService currentUsers;
    @Mock SelfProfileRepository people;
    @Mock TechnicianProfileRepository profiles;
    @Mock SelfProfileRepository.PersonContext person;
    TechnicianProfileService service;
    UUID technicianId;

    @BeforeEach
    void setup() {
        service = new TechnicianProfileService(currentUsers, people, profiles);
        technicianId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        lenient().when(currentUsers.getProvisionedCurrentUser()).thenReturn(user);
        lenient().when(people.findPerson(user.getId())).thenReturn(Optional.of(person));
        lenient().when(person.getTechnicianId()).thenReturn(technicianId);
        lenient().when(profiles.findById(technicianId)).thenReturn(Optional.empty());
        lenient().when(profiles.save(any(TechnicianProfile.class))).thenAnswer(call -> call.getArgument(0));
    }

    private static TechnicianProfileRequest.Address address(String zip, String state) {
        return new TechnicianProfileRequest.Address(zip, " Rua 7 ", "120", " sala 2 ", "Centro", "Goiânia", state);
    }

    private static TechnicianProfileRequest request(TechnicianProfileRequest.Address address, Set<TechnicianSpecialty> specialties) {
        return new TechnicianProfileRequest("  Instalo painéis há anos  ", 8, 60, address, specialties);
    }

    @Test
    void accountsWithoutATechnicianHaveNoProfile() {
        when(person.getTechnicianId()).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> service.get());
    }

    @Test
    void anUntouchedProfileIsEmptyAndListsWhatIsMissing() {
        TechnicianProfileResponse response = service.get();

        assertFalse(response.completed());
        assertEquals(0, response.progress());
        assertEquals(java.util.List.of("ADDRESS", "SPECIALTIES"), response.missing());
        assertTrue(response.specialties().isEmpty());
    }

    @Test
    void aDraftCanBeSavedWithoutTheAddress() {
        TechnicianProfileResponse response = service.save(request(null, Set.of(TechnicianSpecialty.CLEANING)));

        assertFalse(response.completed());
        assertEquals(java.util.List.of("ADDRESS"), response.missing());
        assertEquals("Instalo painéis há anos", response.bio());
        assertEquals(80, response.progress());
    }

    @Test
    void savingNormalizesTheAddress() {
        TechnicianProfileResponse response = service.save(request(address("74000-140", "go"), Set.of(TechnicianSpecialty.INSTALLATION)));

        assertEquals("74000140", response.address().zipCode());
        assertEquals("Rua 7", response.address().street());
        assertEquals("sala 2", response.address().complement());
        assertEquals("GO", response.address().state());
        assertTrue(response.missing().isEmpty());
        assertEquals(100, response.progress());
    }

    @Test
    void savingReplacesEverythingThatWasSent() {
        service.save(request(address("74000140", "GO"), Set.of(TechnicianSpecialty.INSTALLATION, TechnicianSpecialty.CLEANING)));
        var saved = ArgumentCaptor.forClass(TechnicianProfile.class);
        verify(profiles).save(saved.capture());
        when(profiles.findById(technicianId)).thenReturn(Optional.of(saved.getValue()));

        TechnicianProfileResponse response = service.save(new TechnicianProfileRequest(null, null, null, null, Set.of(TechnicianSpecialty.OTHER)));

        assertNull(response.bio());
        assertNull(response.address().street());
        assertEquals(Set.of(TechnicianSpecialty.OTHER), response.specialties());
    }

    @Test
    void completionNeedsTheAddressAndASpecialty() {
        BusinessRuleException error = assertThrows(BusinessRuleException.class, () -> service.complete());

        assertTrue(error.getMessage().contains("ADDRESS"));
        assertTrue(error.getMessage().contains("SPECIALTIES"));
        verify(profiles, never()).save(any());
    }

    @Test
    void aStateThatDoesNotExistDoesNotCountAsAnAddress() {
        service.save(request(address("74000140", "XX"), Set.of(TechnicianSpecialty.INSTALLATION)));
        var saved = ArgumentCaptor.forClass(TechnicianProfile.class);
        verify(profiles).save(saved.capture());
        when(profiles.findById(technicianId)).thenReturn(Optional.of(saved.getValue()));

        assertThrows(BusinessRuleException.class, () -> service.complete());
    }

    @Test
    void aCompleteProfileIsMarkedAsDone() {
        service.save(request(address("74000140", "GO"), Set.of(TechnicianSpecialty.INSTALLATION)));
        var saved = ArgumentCaptor.forClass(TechnicianProfile.class);
        verify(profiles).save(saved.capture());
        when(profiles.findById(technicianId)).thenReturn(Optional.of(saved.getValue()));

        TechnicianProfileResponse response = service.complete();

        assertTrue(response.completed());
        assertNotNull(response.completedAt());
    }

    @Test
    void editingAFinishedProfileIntoAnIncompleteOneTakesItBackToDraft() {
        service.save(request(address("74000140", "GO"), Set.of(TechnicianSpecialty.INSTALLATION)));
        var saved = ArgumentCaptor.forClass(TechnicianProfile.class);
        verify(profiles).save(saved.capture());
        TechnicianProfile profile = saved.getValue();
        when(profiles.findById(technicianId)).thenReturn(Optional.of(profile));
        assertTrue(service.complete().completed());

        TechnicianProfileResponse response = service.save(request(null, Set.of(TechnicianSpecialty.INSTALLATION)));

        assertFalse(response.completed());
        assertNull(response.completedAt());
    }

    @Test
    void completingTwiceKeepsTheFirstCompletionDate() {
        service.save(request(address("74000140", "GO"), Set.of(TechnicianSpecialty.INSTALLATION)));
        var saved = ArgumentCaptor.forClass(TechnicianProfile.class);
        verify(profiles).save(saved.capture());
        when(profiles.findById(technicianId)).thenReturn(Optional.of(saved.getValue()));

        var first = service.complete().completedAt();

        assertEquals(first, service.complete().completedAt());
    }
}
