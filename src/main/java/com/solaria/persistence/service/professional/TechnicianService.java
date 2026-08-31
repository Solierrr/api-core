package com.solaria.persistence.service.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.shared.Contact;
import com.solaria.persistence.domain.entity.identity.Person;
import com.solaria.persistence.domain.entity.professional.Technician;
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


@Service
public class TechnicianService {

    private final TechnicianRepository technicianRepository;
    private final PersonRepository personRepository;

    public TechnicianService(TechnicianRepository technicianRepository,
                             PersonRepository personRepository) {
        this.technicianRepository = technicianRepository;
        this.personRepository = personRepository;
    }

    @Transactional
    public TechnicianResponseDTO save(TechnicianRequestDTO dto) {
        Person person = personRepository.findById(dto.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pessoa não encontrada com ID: " + dto.getPersonId()));

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
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Técnico com id:" + id + " não encontrado para atualização"));
        if (!technician.getPerson().getId().equals(dto.getPersonId())) {
            throw new InvalidFieldException("Pessoa (personId) imutável: " + dto.getPersonId());
        }

        technician.setCrea(dto.getCrea());

        return toResponse(technicianRepository.save(technician));
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!technicianRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Técnico com id:" + id + " não encontrado para exclusão");
        }

        technicianRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public TechnicianResponseDTO findById(UUID id) {
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Técnico não encontrado com ID: " + id));
        return toResponse(technician);
    }

    @Transactional(readOnly = true)
    public List<TechnicianResponseDTO> findAll() {
        return technicianRepository.findAll().stream().map(this::toResponse).toList();
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
