package com.solaria.persistence.service.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.shared.Contact;
import com.solaria.persistence.domain.entity.identity.Person;
import com.solaria.persistence.domain.entity.professional.Technician;
import com.solaria.persistence.domain.enums.professional.TechnicianStatus;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.dto.request.professional.TechnicianRequestDTO;
import com.solaria.persistence.dto.response.shared.ContactResponseDTO;
import com.solaria.persistence.dto.response.identity.PersonResponseDTO;
import com.solaria.persistence.dto.response.professional.TechnicianResponseDTO;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.identity.PersonRepository;
import com.solaria.persistence.repository.professional.TechnicianRepository;
import com.solaria.persistence.util.SlugUtil;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;


@Service
public class TechnicianService {

    private final TechnicianRepository technicianRepository;
    private final PersonRepository personRepository;
    private final CurrentUserService currentUserService;
    private final RbacAuthorizationService rbac;
    private final ApprovedProfessionalGuard approvedGuard;

    public TechnicianService(TechnicianRepository technicianRepository,
                             PersonRepository personRepository,
                             CurrentUserService currentUserService,
                             RbacAuthorizationService rbac,
                             ApprovedProfessionalGuard approvedGuard) {
        this.technicianRepository = technicianRepository;
        this.personRepository = personRepository;
        this.currentUserService = currentUserService;
        this.rbac = rbac;
        this.approvedGuard = approvedGuard;
    }

    @Transactional
    public TechnicianResponseDTO save(TechnicianRequestDTO dto) {
        currentUserService.getProvisionedCurrentUserId();
        Person person = personRepository.findById(dto.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pessoa não encontrada com ID: " + dto.getPersonId()));

        rbac.requireOwnUser(person.getUser() == null ? null : person.getUser().getId());

        if (technicianRepository.existsByPersonId(dto.getPersonId())) {
            throw new DuplicateResourceException(
                    "Técnico já cadastrado para a pessoa: " + dto.getPersonId());
        }

        Technician technician = new Technician();
        technician.setPerson(person);
        technician.setCrea(dto.getCrea());
        technician.setSlug(generateUniqueSlug(person.getName()));

        return toResponse(technicianRepository.save(technician));
    }

    @Transactional
    public TechnicianResponseDTO update(UUID id, TechnicianRequestDTO dto) {
        currentUserService.getProvisionedCurrentUserId();
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Técnico com id:" + id + " não encontrado para atualização"));
        requireOwner(technician);
        if (!technician.getPerson().getId().equals(dto.getPersonId())) {
            throw new InvalidFieldException("Pessoa (personId) imutável: " + dto.getPersonId());
        }

        technician.setCrea(dto.getCrea());

        return toResponse(technicianRepository.save(technician));
    }

    @Transactional
    public TechnicianResponseDTO decide(UUID id, boolean approved) {
        Technician technician = technicianRepository.findByIdForUpdate(id).orElseThrow(
                () -> new ResourceNotFoundException("Profissional não encontrado"));
        approvedGuard.requireLegacyChangesAllowed(technician.getPerson().getUser().getId());
        if (technician.getStatus() != TechnicianStatus.UNDER_ANALYSIS) {
            throw new BusinessRuleException("Somente profissionais em análise podem receber uma decisão");
        }
        technician.setStatus(approved ? TechnicianStatus.APPROVED : TechnicianStatus.REJECTED);
        return toResponse(technicianRepository.save(technician));
    }

    @Transactional
    public void deleteById(UUID id) {
        currentUserService.getProvisionedCurrentUserId();
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico não encontrado com ID: " + id));
        requireOwner(technician);
        technicianRepository.delete(technician);
    }

    @Transactional(readOnly = true)
    public TechnicianResponseDTO findById(UUID id) {
        currentUserService.getProvisionedCurrentUserId();
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Técnico não encontrado com ID: " + id));
        requireOwner(technician);
        return toResponse(technician);
    }

    @Transactional(readOnly = true)
    public List<TechnicianResponseDTO> findAll() {
        UUID userId = currentUserService.getProvisionedCurrentUserId();
        return technicianRepository.findByPersonUserId(userId).stream().map(this::toResponse).toList();
    }

    private void requireOwner(Technician technician) {
        Person person = technician.getPerson();
        rbac.requireOwnUser(person.getUser() == null ? null : person.getUser().getId());
    }

    private String generateUniqueSlug(String name) {
        String base = SlugUtil.slugify(name);
        String candidate = base;
        int suffix = 2;
        while (technicianRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private TechnicianResponseDTO toResponse(Technician technician) {
        TechnicianResponseDTO response = new TechnicianResponseDTO();
        response.setId(technician.getId());
        response.setStatus(technician.getStatus());
        response.setCrea(technician.getCrea());
        response.setSlug(technician.getSlug());
        response.setPerson(toPersonResponse(technician.getPerson()));
        return response;
    }

    private PersonResponseDTO toPersonResponse(Person person) {
        if (person == null) {
            return null;
        }
        PersonResponseDTO response = new PersonResponseDTO();
        response.setId(person.getId());
        response.setName(person.getName());
        response.setCpf(person.getCpf());
        response.setBirthDate(person.getBirthDate());
        response.setUserId(person.getUser().getId());
        response.setContact(toContactResponse(person.getContact()));
        return response;
    }

    private ContactResponseDTO toContactResponse(Contact contact) {
        if (contact == null) {
            return null;
        }
        ContactResponseDTO dto = new ContactResponseDTO();
        dto.setId(contact.getId());
        dto.setEmail(contact.getEmail());
        dto.setPhone(contact.getPhone());
        return dto;
    }
}
