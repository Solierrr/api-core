package com.solaria.persistence.service.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.enums.professional.TechnicalAffiliationType;
import com.solaria.persistence.domain.enums.shared.PhotoType;
import com.solaria.persistence.dto.response.identity.SelfProfileResponseDTO;
import com.solaria.persistence.dto.response.identity.SelfProfileResponseDTO.ActiveAffiliation;
import com.solaria.persistence.dto.response.identity.SelfProfileResponseDTO.CompanyMembership;
import com.solaria.persistence.dto.response.identity.SelfProfileResponseDTO.PersonContext;
import com.solaria.persistence.dto.response.identity.SelfProfileResponseDTO.ProfessionSummary;
import com.solaria.persistence.dto.response.identity.SelfProfileResponseDTO.TechnicianContext;
import com.solaria.persistence.repository.identity.SelfProfileRepository;
import com.solaria.persistence.security.CurrentUserService;

@Service
public class SelfProfileService {

    private final CurrentUserService currentUserService;
    private final SelfProfileRepository repository;

    public SelfProfileService(CurrentUserService currentUserService, SelfProfileRepository repository) {
        this.currentUserService = currentUserService;
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public SelfProfileResponseDTO getProfile() {
        User user = currentUserService.getProvisionedCurrentUser();
        SelfProfileRepository.PersonContext personRow = repository.findPerson(user.getId()).orElse(null);
        PersonContext person = personRow == null ? null : new PersonContext(personRow.getName(), personRow.getPhone());
        UUID technicianId = personRow == null ? null : personRow.getTechnicianId();
        TechnicianContext technician = null;
        List<ActiveAffiliation> affiliations = List.of();
        if (technicianId != null) {
            List<ProfessionSummary> professions = repository.findProfessions(technicianId).stream()
                    .map(row -> new ProfessionSummary(row.getId(), row.getName()))
                    .toList();
            technician = new TechnicianContext(technicianId, professions);
            affiliations = repository.findActiveAffiliations(technicianId).stream()
                    .map(row -> row.getType() == TechnicalAffiliationType.INDEPENDENT
                            ? new ActiveAffiliation(row.getType(), null, null)
                            : new ActiveAffiliation(row.getType(), row.getCompanyId(), row.getCompanyName()))
                    .toList();
        }
        List<CompanyMembership> memberships = repository.findCompanyMemberships(user.getId()).stream()
                .map(row -> new CompanyMembership(row.getCompanyId(), row.getCompanyName(), row.getPosition()))
                .toList();
        String avatar = user.getAvatar();
        String banner = user.getBanner();
        for (SelfProfileRepository.PhotoSummary photo : repository.findPhotos(
                user.getId(), List.of(PhotoType.PROFILE, PhotoType.BANNER))) {
            if (photo.getType() == PhotoType.PROFILE) {
                avatar = photo.getUrl();
            } else if (photo.getType() == PhotoType.BANNER) {
                banner = photo.getUrl();
            }
        }
        return new SelfProfileResponseDTO(user.getId(), user.getAuth_id(), avatar, banner,
                person, technician, affiliations, memberships);
    }
}
