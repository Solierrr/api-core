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
import com.solaria.persistence.repository.identity.UserRepository;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final TechnicianRepository technicianRepository;

    public PersonService(PersonRepository personRepository,
                         UserRepository userRepository,
                         ContactRepository contactRepository,
                         TechnicianRepository technicianRepository) {
        this.personRepository = personRepository;
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
        this.technicianRepository = technicianRepository;
    }

    @Transactional
    public PersonResponseDTO save(PersonRequestDTO dto) {
        validateBirthDate(dto.getBirthDate());

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado com ID: " + dto.getUserId()));

        Contact contact = contactRepository.findById(dto.getContactId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contato não encontrado com ID: " + dto.getContactId()));

        if (personRepository.existsByCpf(dto.getCpf())) {
            throw new DuplicateResourceException(
                    "Pessoa já cadastrada para o CPF: " + dto.getCpf());
        }

        if (personRepository.existsByUserId(dto.getUserId())) {
            throw new DuplicateResourceException(
                    "Pessoa já cadastrada para o usuário: " + dto.getUserId());
        }

        Person person = new Person();
        person.setUser(user);
        person.setContact(contact);
        person.setName(dto.getName());
        person.setCpf(dto.getCpf());
        person.setBirthDate(dto.getBirthDate());

        return toResponse(personRepository.save(person));
    }

    @Transactional
    public PersonResponseDTO update(UUID id, PersonRequestDTO dto) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pessoa com id:" + id + " não encontrada para atualização"));

        if (!person.getUser().getId().equals(dto.getUserId())) {
            throw new InvalidFieldException("Usuário (userId) imutável: " + dto.getUserId());
        }

        validateBirthDate(dto.getBirthDate());

        if (personRepository.existsByCpfAndIdNot(dto.getCpf(), id)) {
            throw new DuplicateResourceException(
                    "Pessoa já cadastrada para o CPF: " + dto.getCpf());
        }

        Contact contact = contactRepository.findById(dto.getContactId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contato não encontrado com ID: " + dto.getContactId()));

        person.setContact(contact);
        person.setName(dto.getName());
        person.setCpf(dto.getCpf());
        person.setBirthDate(dto.getBirthDate());

        return toResponse(personRepository.save(person));
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!personRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Pessoa com id:" + id + " não encontrada para exclusão");
        }

        if (technicianRepository.existsByPersonId(id)) {
            throw new ResourceInUseException(
                    "Pessoa não pode ser excluída: possui técnico vinculado");
        }

        personRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PersonResponseDTO findById(UUID id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pessoa não encontrada com ID: " + id));
        return toResponse(person);
    }

    @Transactional(readOnly = true)
    public List<PersonResponseDTO> findAll() {
        return personRepository.findAll().stream().map(this::toResponse).toList();
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
