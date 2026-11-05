package com.solaria.persistence.service.professional;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.identity.UserPhoto;
import com.solaria.persistence.domain.entity.professional.ProfessionalRegistration;
import com.solaria.persistence.domain.entity.professional.Technician;
import com.solaria.persistence.dto.response.professional.CatalogTechnicianResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.domain.enums.shared.PhotoType;
import com.solaria.persistence.repository.identity.UserPhotoRepository;
import com.solaria.persistence.repository.professional.ProfessionalRegistrationRepository;
import com.solaria.persistence.repository.professional.TechnicianRepository;
import com.solaria.persistence.domain.enums.professional.TechnicianStatus;

@Service
public class TechnicianCatalogService {

    private final TechnicianRepository technicianRepository;
    private final ProfessionalRegistrationRepository professionalRegistrationRepository;

    private final UserPhotoRepository userPhotoRepository;

    public TechnicianCatalogService(TechnicianRepository technicianRepository,
                                     ProfessionalRegistrationRepository professionalRegistrationRepository,
                                     UserPhotoRepository userPhotoRepository) {
        this.technicianRepository = technicianRepository;
        this.professionalRegistrationRepository = professionalRegistrationRepository;
        this.userPhotoRepository = userPhotoRepository;
    }

    @Transactional(readOnly = true)
    public List<CatalogTechnicianResponseDTO> findAll() {
        return technicianRepository.findAll().stream().filter(item -> item.getStatus() == TechnicianStatus.APPROVED).map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CatalogTechnicianResponseDTO findBySlug(String slug) {
        Technician technician = technicianRepository.findBySlug(slug)
                .filter(item -> item.getStatus() == TechnicianStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));
        return toResponse(technician);
    }

    private String photoUrl(List<UserPhoto> photos, PhotoType type) {
        return photos.stream().filter(photo -> photo.getType() == type).map(UserPhoto::getUrl).findFirst().orElse(null);
    }

    private CatalogTechnicianResponseDTO toResponse(Technician technician) {
        List<String> professions = professionalRegistrationRepository.findByTechnicianId(technician.getId())
                .stream()
                .map(ProfessionalRegistration::getProfession)
                .filter(profession -> profession != null)
                .map(profession -> profession.getName())
                .distinct()
                .toList();
        var userId = technician.getPerson().getUser() == null ? null : technician.getPerson().getUser().getId();
        List<UserPhoto> photos = userId == null ? List.of() : userPhotoRepository.findByUserIdOrderByTypeAsc(userId);
        return new CatalogTechnicianResponseDTO(
                technician.getId(),
                technician.getSlug(),
                technician.getPerson().getName(),
                technician.getCrea(),
                professions,
                photoUrl(photos, PhotoType.PROFILE),
                photoUrl(photos, PhotoType.BANNER));
    }
}
