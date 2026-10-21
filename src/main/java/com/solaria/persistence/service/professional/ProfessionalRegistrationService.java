package com.solaria.persistence.service.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.professional.ProfessionalRegistration;
import com.solaria.persistence.domain.entity.identity.Person;
import com.solaria.persistence.domain.entity.professional.Profession;
import com.solaria.persistence.domain.entity.professional.Technician;
import com.solaria.persistence.dto.request.professional.ProfessionalRegistrationRequestDTO;
import com.solaria.persistence.dto.response.professional.ProfessionalRegistrationResponseDTO;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;
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
    private final com.solaria.persistence.service.professional.ApprovedProfessionalGuard approvedGuard;
    private final CurrentUserService currentUserService;
    private final RbacAuthorizationService rbac;

    public ProfessionalRegistrationService(ProfessionalRegistrationRepository professionalRegistrationRepository,
                                           TechnicianRepository technicianRepository,
                                           ProfessionRepository professionRepository,
                                           CertificationRecordRepository certificationRecordRepository,
                                           CurrentUserService currentUserService,
                                           RbacAuthorizationService rbac,
                                           com.solaria.persistence.service.professional.ApprovedProfessionalGuard approvedGuard) {
        this.professionalRegistrationRepository = professionalRegistrationRepository;
        this.technicianRepository = technicianRepository;
        this.professionRepository = professionRepository;
        this.certificationRecordRepository = certificationRecordRepository;
        this.currentUserService = currentUserService;
        this.rbac = rbac;
        this.approvedGuard = approvedGuard;
    }

    @Transactional
    public ProfessionalRegistrationResponseDTO save(ProfessionalRegistrationRequestDTO dto) {
        currentUserService.getProvisionedCurrentUserId();
        rbac.requireOwnUser(resolveTechnician(dto.getTechnicianId()).getPerson().getUser().getId());
        ProfessionalRegistration professionalRegistration = new ProfessionalRegistration();
        approvedGuard.requireLegacyChangesAllowed(resolveTechnician(dto.getTechnicianId()).getPerson().getUser().getId());
        professionalRegistration.setTechnician(resolveTechnician(dto.getTechnicianId()));
        professionalRegistration.setProfession(resolveProfession(dto.getProfessionId()));
        professionalRegistration.setCouncil(dto.getCouncil());
        professionalRegistration.setNumber(dto.getNumber());
        professionalRegistration.setExpirationDate(dto.getExpirationDate());

        return toResponse(professionalRegistrationRepository.save(professionalRegistration));
    }

    @Transactional
    public ProfessionalRegistrationResponseDTO update(UUID id, ProfessionalRegistrationRequestDTO dto) {
        currentUserService.getProvisionedCurrentUserId();
        ProfessionalRegistration professionalRegistration = professionalRegistrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registro Profissional com id:" + id + " não encontrado para atualização"));
        requireOwner(professionalRegistration);

        approvedGuard.requireLegacyChangesAllowed(resolveTechnician(dto.getTechnicianId()).getPerson().getUser().getId());
        if (professionalRegistration.getTechnician() != null) {
            approvedGuard.requireLegacyChangesAllowed(professionalRegistration.getTechnician().getPerson().getUser().getId());
            rbac.requireOwnUser(professionalRegistration.getTechnician().getPerson().getUser().getId());
            if (!professionalRegistration.getTechnician().getId().equals(dto.getTechnicianId())) {
                throw new com.solaria.persistence.exception.InvalidFieldException("Technician cannot be changed");
            }
        }
        professionalRegistration.setTechnician(resolveTechnician(dto.getTechnicianId()));
        professionalRegistration.setProfession(resolveProfession(dto.getProfessionId()));
        professionalRegistration.setCouncil(dto.getCouncil());
        professionalRegistration.setNumber(dto.getNumber());
        professionalRegistration.setExpirationDate(dto.getExpirationDate());

        return toResponse(professionalRegistrationRepository.save(professionalRegistration));
    }

    @Transactional
    public void deleteById(UUID id) {
        currentUserService.getProvisionedCurrentUserId();
        ProfessionalRegistration professionalRegistration = professionalRegistrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado com ID: " + id));
        requireOwner(professionalRegistration);

        if (certificationRecordRepository.existsByProfessionalRegistrationId(id)) {
            throw new ResourceInUseException(
                    "Registro Profissional não pode ser excluído: possui registro de certificação vinculado");
        }

        var registration = professionalRegistrationRepository.findById(id).orElseThrow();
        if (registration.getTechnician() != null) approvedGuard.requireLegacyChangesAllowed(registration.getTechnician().getPerson().getUser().getId());
        professionalRegistrationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ProfessionalRegistrationResponseDTO findById(UUID id) {
        currentUserService.getProvisionedCurrentUserId();
        ProfessionalRegistration professionalRegistration = professionalRegistrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registro Profissional não encontrado com ID: " + id));
        requireOwner(professionalRegistration);
        return toResponse(professionalRegistration);
    }

    @Transactional(readOnly = true)
    public List<ProfessionalRegistrationResponseDTO> findByTechnician(UUID technicianId) {
        UUID userId = currentUserService.getProvisionedCurrentUserId();
        resolveTechnician(technicianId);
        return professionalRegistrationRepository
                .findByTechnicianIdAndTechnicianPersonUserId(technicianId, userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void requireOwner(ProfessionalRegistration entity) {
        rbac.requireOwnUser(professionalRegistrationRepository.findOwnerUserId(entity.getId()).orElse(null));
    }

    private Technician resolveTechnician(UUID technicianId) {
        if (technicianId == null) {
            throw new InvalidFieldException("technicianId obrigatório para identificar o titular");
        }
        Technician parent = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Técnico não encontrado com ID: " + technicianId));
        Person person = parent.getPerson();
        rbac.requireOwnUser(person.getUser() == null ? null : person.getUser().getId());
        return parent;
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
