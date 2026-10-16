package com.solaria.persistence.service.professional;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.professional.ProfessionalRegistration;
import com.solaria.persistence.domain.entity.professional.Technician;
import com.solaria.persistence.dto.response.professional.CatalogTechnicianResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.professional.ProfessionalRegistrationRepository;
import com.solaria.persistence.repository.professional.TechnicianRepository;

@Service
public class TechnicianCatalogService {

    private final TechnicianRepository technicianRepository;
    private final ProfessionalRegistrationRepository professionalRegistrationRepository;

    public TechnicianCatalogService(TechnicianRepository technicianRepository,
                                     ProfessionalRegistrationRepository professionalRegistrationRepository) {
        this.technicianRepository = technicianRepository;
        this.professionalRegistrationRepository = professionalRegistrationRepository;
    }

    @Transactional(readOnly = true)
    public List<CatalogTechnicianResponseDTO> findAll() {
        return technicianRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CatalogTechnicianResponseDTO findBySlug(String slug) {
        Technician technician = technicianRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));
        return toResponse(technician);
    }

    private CatalogTechnicianResponseDTO toResponse(Technician technician) {
        List<String> professions = professionalRegistrationRepository.findByTechnicianId(technician.getId())
                .stream()
                .map(ProfessionalRegistration::getProfession)
                .filter(profession -> profession != null)
                .map(profession -> profession.getName())
                .distinct()
                .toList();
        return new CatalogTechnicianResponseDTO(
                technician.getId(),
                technician.getSlug(),
                technician.getPerson().getName(),
                technician.getCrea(),
                professions);
    }
}
