package com.solaria.persistence.service.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.professional.ProfessionalRegistration;
import com.solaria.persistence.domain.entity.professional.Profession;
import com.solaria.persistence.domain.entity.professional.Technician;
import com.solaria.persistence.dto.request.professional.ProfessionalRegistrationRequestDTO;
import com.solaria.persistence.dto.response.professional.ProfessionalRegistrationResponseDTO;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.professional.CertificationRecordRepository;
import com.solaria.persistence.repository.professional.ProfessionRepository;
import com.solaria.persistence.repository.professional.ProfessionalRegistrationRepository;
import com.solaria.persistence.repository.professional.TechnicianRepository;


@Service
public class ProfessionalRegistrationService {

    private final ProfessionalRegistrationRepository professionalRegistrationRepository;
    private final TechnicianRepository technicianRepository;
    private final ProfessionRepository professionRepository;
    private final CertificationRecordRepository certificationRecordRepository;

    public ProfessionalRegistrationService(ProfessionalRegistrationRepository professionalRegistrationRepository,
                                           TechnicianRepository technicianRepository,
                                           ProfessionRepository professionRepository,
                                           CertificationRecordRepository certificationRecordRepository) {
        this.professionalRegistrationRepository = professionalRegistrationRepository;
        this.technicianRepository = technicianRepository;
        this.professionRepository = professionRepository;
        this.certificationRecordRepository = certificationRecordRepository;
    }

    @Transactional
    public ProfessionalRegistrationResponseDTO save(ProfessionalRegistrationRequestDTO dto) {
        ProfessionalRegistration professionalRegistration = new ProfessionalRegistration();
        professionalRegistration.setTechnician(resolveTechnician(dto.getTechnicianId()));
        professionalRegistration.setProfession(resolveProfession(dto.getProfessionId()));
        professionalRegistration.setCouncil(dto.getCouncil());
        professionalRegistration.setNumber(dto.getNumber());
        professionalRegistration.setExpirationDate(dto.getExpirationDate());

        return toResponse(professionalRegistrationRepository.save(professionalRegistration));
    }

    @Transactional
    public ProfessionalRegistrationResponseDTO update(UUID id, ProfessionalRegistrationRequestDTO dto) {
        ProfessionalRegistration professionalRegistration = professionalRegistrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registro Profissional com id:" + id + " não encontrado para atualização"));

        professionalRegistration.setTechnician(resolveTechnician(dto.getTechnicianId()));
        professionalRegistration.setProfession(resolveProfession(dto.getProfessionId()));
        professionalRegistration.setCouncil(dto.getCouncil());
        professionalRegistration.setNumber(dto.getNumber());
        professionalRegistration.setExpirationDate(dto.getExpirationDate());

        return toResponse(professionalRegistrationRepository.save(professionalRegistration));
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!professionalRegistrationRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Registro Profissional com id:" + id + " não encontrado para exclusão");
        }

        if (certificationRecordRepository.existsByProfessionalRegistrationId(id)) {
            throw new ResourceInUseException(
                    "Registro Profissional não pode ser excluído: possui registro de certificação vinculado");
        }

        professionalRegistrationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ProfessionalRegistrationResponseDTO findById(UUID id) {
        ProfessionalRegistration professionalRegistration = professionalRegistrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registro Profissional não encontrado com ID: " + id));
        return toResponse(professionalRegistration);
    }

    @Transactional(readOnly = true)
    public List<ProfessionalRegistrationResponseDTO> findByTechnician(UUID technicianId) {
        return professionalRegistrationRepository.findByTechnicianId(technicianId).stream()
                .map(this::toResponse)
                .toList();
    }

    private Technician resolveTechnician(UUID technicianId) {
        if (technicianId == null) {
            return null;
        }
        return technicianRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Técnico não encontrado com ID: " + technicianId));
    }

    private Profession resolveProfession(UUID professionId) {
        if (professionId == null) {
            return null;
        }
        return professionRepository.findById(professionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profissão não encontrada com ID: " + professionId));
    }

    private ProfessionalRegistrationResponseDTO toResponse(ProfessionalRegistration entity) {
        ProfessionalRegistrationResponseDTO response = new ProfessionalRegistrationResponseDTO();
        response.setId(entity.getId());
        response.setTechnicianId(entity.getTechnician() == null ? null : entity.getTechnician().getId());
        response.setProfessionId(entity.getProfession() == null ? null : entity.getProfession().getId());
        response.setCouncil(entity.getCouncil());
        response.setNumber(entity.getNumber());
        response.setExpirationDate(entity.getExpirationDate());
        return response;
    }
}
