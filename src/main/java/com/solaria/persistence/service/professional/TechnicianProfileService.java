package com.solaria.persistence.service.professional;

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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The signed-in technician's own profile. Saving is always allowed, even with the address missing, and only keeps a
 * draft; {@link #complete()} is what marks the profile as done, and only when the required data is present: a complete
 * address, at least one specialty (described, when it is "other") and the years of experience.
 */
@Service
@Transactional
public class TechnicianProfileService {
    static final String ADDRESS = "ADDRESS";
    static final String SPECIALTIES = "SPECIALTIES";
    static final String OTHER_SPECIALTY = "OTHER_SPECIALTY";
    static final String EXPERIENCE = "EXPERIENCE";
    private static final int ITEMS = 5;
    private static final Set<String> STATES = Set.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR",
            "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    private final CurrentUserService currentUsers;
    private final SelfProfileRepository people;
    private final TechnicianProfileRepository profiles;

    public TechnicianProfileService(
            CurrentUserService currentUsers, SelfProfileRepository people, TechnicianProfileRepository profiles) {
        this.currentUsers = currentUsers;
        this.people = people;
        this.profiles = profiles;
    }

    @Transactional(readOnly = true)
    public TechnicianProfileResponse get() {
        UUID technicianId = technicianId();
        return response(profiles.findById(technicianId).orElseGet(() -> blank(technicianId)));
    }

    public TechnicianProfileResponse save(TechnicianProfileRequest request) {
        UUID technicianId = technicianId();
        TechnicianProfile profile = profiles.findById(technicianId).orElseGet(() -> blank(technicianId));
        apply(profile, request);
        // Editing a finished profile into an incomplete one takes it back to draft.
        if (profile.getCompletedAt() != null && !missing(profile).isEmpty()) {
            profile.setCompletedAt(null);
        }
        profile.setUpdatedAt(OffsetDateTime.now());
        return response(profiles.save(profile));
    }

    public TechnicianProfileResponse complete() {
        UUID technicianId = technicianId();
        TechnicianProfile profile = profiles.findById(technicianId).orElseGet(() -> blank(technicianId));
        List<String> missing = missing(profile);
        if (!missing.isEmpty()) {
            throw new BusinessRuleException("Perfil incompleto: " + String.join(", ", missing));
        }
        if (profile.getCompletedAt() == null) {
            profile.setCompletedAt(OffsetDateTime.now());
            profile.setUpdatedAt(OffsetDateTime.now());
        }
        return response(profiles.save(profile));
    }

    private UUID technicianId() {
        User user = currentUsers.getProvisionedCurrentUser();
        return people.findPerson(user.getId())
                .map(SelfProfileRepository.PersonContext::getTechnicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil profissional não encontrado"));
    }

    private static TechnicianProfile blank(UUID technicianId) {
        TechnicianProfile profile = new TechnicianProfile();
        profile.setTechnicianId(technicianId);
        return profile;
    }

    private static void apply(TechnicianProfile profile, TechnicianProfileRequest r) {
        profile.setBio(text(r.bio()));
        profile.setYearsExperience(r.yearsExperience());
        profile.setServiceRadiusKm(r.serviceRadiusKm());
        TechnicianProfileRequest.Address a = r.address();
        profile.setZipCode(a == null || a.zipCode() == null ? null : text(a.zipCode().replace("-", "")));
        profile.setStreet(a == null ? null : text(a.street()));
        profile.setNumber(a == null ? null : text(a.number()));
        profile.setComplement(a == null ? null : text(a.complement()));
        profile.setNeighborhood(a == null ? null : text(a.neighborhood()));
        profile.setCity(a == null ? null : text(a.city()));
        profile.setState(a == null || a.state() == null ? null : text(a.state().toUpperCase()));
        Set<TechnicianSpecialty> specialties = EnumSet.noneOf(TechnicianSpecialty.class);
        if (r.specialties() != null) specialties.addAll(r.specialties());
        profile.getSpecialties().clear();
        profile.getSpecialties().addAll(specialties);
        profile.setOtherSpecialty(specialties.contains(TechnicianSpecialty.OTHER) ? text(r.otherSpecialty()) : null);
    }

    private static String text(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean hasAddress(TechnicianProfile p) {
        return p.getZipCode() != null
                && p.getStreet() != null
                && p.getNumber() != null
                && p.getNeighborhood() != null
                && p.getCity() != null
                && p.getState() != null
                && STATES.contains(p.getState());
    }

    private static boolean needsOtherSpecialty(TechnicianProfile p) {
        return p.getSpecialties().contains(TechnicianSpecialty.OTHER) && p.getOtherSpecialty() == null;
    }

    static List<String> missing(TechnicianProfile p) {
        List<String> missing = new ArrayList<>();
        if (!hasAddress(p)) missing.add(ADDRESS);
        if (p.getSpecialties().isEmpty()) missing.add(SPECIALTIES);
        if (needsOtherSpecialty(p)) missing.add(OTHER_SPECIALTY);
        if (p.getYearsExperience() == null) missing.add(EXPERIENCE);
        return missing;
    }

    private static int progress(TechnicianProfile p) {
        int done = 0;
        if (hasAddress(p)) done++;
        if (!p.getSpecialties().isEmpty() && !needsOtherSpecialty(p)) done++;
        if (p.getYearsExperience() != null) done++;
        if (p.getServiceRadiusKm() != null) done++;
        if (p.getBio() != null) done++;
        return done * 100 / ITEMS;
    }

    private static TechnicianProfileResponse response(TechnicianProfile p) {
        return new TechnicianProfileResponse(
                p.getTechnicianId(),
                p.getBio(),
                p.getYearsExperience(),
                p.getServiceRadiusKm(),
                new TechnicianProfileResponse.Address(
                        p.getZipCode(), p.getStreet(), p.getNumber(), p.getComplement(),
                        p.getNeighborhood(), p.getCity(), p.getState()),
                EnumSet.copyOf(p.getSpecialties().isEmpty() ? EnumSet.noneOf(TechnicianSpecialty.class) : p.getSpecialties()),
                p.getOtherSpecialty(),
                p.getCompletedAt() != null,
                p.getCompletedAt(),
                progress(p),
                missing(p));
    }
}
