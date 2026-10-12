package com.solaria.persistence.service.identity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.shared.Contact;
import com.solaria.persistence.domain.entity.identity.Person;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.dto.request.identity.PersonRequestDTO;
import com.solaria.persistence.dto.response.shared.ContactResponseDTO;
import com.solaria.persistence.dto.response.identity.PersonResponseDTO;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.shared.ContactRepository;
import com.solaria.persistence.repository.identity.PersonRepository;
import com.solaria.persistence.repository.professional.TechnicianRepository;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import com.solaria.persistence.dto.request.shared.ContactRequestDTO;

@Service
public class PersonService {

    private static final String PERSON_NOT_FOUND = "Pessoa não encontrada com ID: ";

    private final PersonRepository personRepository;
    private final CurrentUserService currentUserService;
    private final RbacAuthorizationService rbac;
    private final ContactRepository contactRepository;
    private final TechnicianRepository technicianRepository;

    public PersonService(PersonRepository personRepository,
                         CurrentUserService currentUserService,
                         RbacAuthorizationService rbac,
                         ContactRepository contactRepository,
                         TechnicianRepository technicianRepository) {
        this.personRepository = personRepository;
        this.currentUserService = currentUserService;
        this.rbac = rbac;
        this.contactRepository = contactRepository;
        this.technicianRepository = technicianRepository;
    }

    @Transactional
    public PersonResponseDTO save(PersonRequestDTO dto) {
        User user = currentUserService.getProvisionedCurrentUser();
        validateBirthDate(dto.getBirthDate());

        if (personRepository.existsByCpf(dto.getCpf())) {
            throw new DuplicateResourceException(
                    "Pessoa já cadastrada para o CPF: " + dto.getCpf());
        }

        if (personRepository.existsByUserId(user.getId())) {
            throw new DuplicateResourceException(
                    "Pessoa já cadastrada para o usuário: " + user.getId());
        }

        Person person = new Person();
        person.setUser(user);
        person.setContact(saveContact(null, dto.getContact()));
        person.setName(dto.getName());
        person.setCpf(dto.getCpf());
        person.setBirthDate(dto.getBirthDate());

        return toResponse(personRepository.save(person));
    }

    @Transactional
    public PersonResponseDTO update(UUID id, PersonRequestDTO dto) {
        currentUserService.getProvisionedCurrentUserId();
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PERSON_NOT_FOUND + id));
        rbac.requireOwnUser(person.getUser() == null ? null : person.getUser().getId());
        if (!person.getCpf().equals(dto.getCpf())) {
            throw new InvalidFieldException("CPF imutável: " + dto.getCpf());
        }
        validateBirthDate(dto.getBirthDate());
        Contact previousContact = person.getContact();
        person.setContact(saveContact(previousContact, dto.getContact()));
        person.setName(dto.getName());
        person.setBirthDate(dto.getBirthDate());

        personRepository.saveAndFlush(person);
        if (dto.getContact() == null && previousContact != null) {
            contactRepository.delete(previousContact);
        }
        return toResponse(person);
    }

    @Transactional
    public void deleteById(UUID id) {
        currentUserService.getProvisionedCurrentUserId();
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PERSON_NOT_FOUND + id));
        rbac.requireOwnUser(person.getUser() == null ? null : person.getUser().getId());
        Contact contact = person.getContact();

        if (technicianRepository.existsByPersonId(id)) {
            throw new ResourceInUseException(
                    "Pessoa não pode ser excluída: possui técnico vinculado");
        }

        personRepository.delete(person);
        personRepository.flush();
        if (contact != null) {
            contactRepository.delete(contact);
        }
    }

    @Transactional(readOnly = true)
    public PersonResponseDTO findById(UUID id) {
        currentUserService.getProvisionedCurrentUserId();
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        PERSON_NOT_FOUND + id));
        rbac.requireOwnUser(person.getUser() == null ? null : person.getUser().getId());
        return toResponse(person);
    }

    @Transactional(readOnly = true)
    public List<PersonResponseDTO> findAll() {
        UUID userId = currentUserService.getProvisionedCurrentUserId();
        return personRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    private Contact saveContact(Contact current, ContactRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        Contact contact = current == null ? new Contact() : current;
        contact.setEmail(dto.getEmail());
        contact.setPhone(dto.getPhone());
        return contactRepository.save(contact);
    }

    private void validateBirthDate(LocalDate birthDate) {
        if (birthDate == null || !birthDate.isBefore(LocalDate.now())) {
            throw new InvalidFieldException("Data de nascimento inválida: " + birthDate);
        }
    }

    private PersonResponseDTO toResponse(Person person) {
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
