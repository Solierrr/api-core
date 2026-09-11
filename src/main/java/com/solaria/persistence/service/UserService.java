package com.solaria.persistence.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.solaria.persistence.domain.entity.User;
import com.solaria.persistence.dto.request.UserRequestDTO;
import com.solaria.persistence.dto.response.UserResponseDTO;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.PersonRepository;
import com.solaria.persistence.repository.UserCompanyRepository;
import com.solaria.persistence.repository.UserRepository;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final ObjectMapper objectMapper;

    public UserService(UserRepository userRepository,
                        PersonRepository personRepository,
                        UserCompanyRepository userCompanyRepository,
                        ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UserResponseDTO save(UserRequestDTO dto) {
        User user = new User();
        user.setAuth_id(dto.getAuthId());
        user.setUsername(dto.getUsername());
        user.setAvatar(dto.getAvatar());
        user.setBanner(dto.getBanner());

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponseDTO update(UUID id, UserRequestDTO dto) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Usuário com id:" + id + " não encontrado para atualização"));
        if (!user.getAuth_id().equals(dto.getAuthId())) {
            throw new InvalidFieldException("ID de autenticação (authId) imutável: " + dto.getAuthId());
        }

        user.setUsername(dto.getUsername());
        user.setAvatar(dto.getAvatar());
        user.setBanner(dto.getBanner());

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário com id:" + id + " não encontrado para exclusão");
        }
        if (personRepository.existsByUserId(id)) {
            throw new ResourceInUseException("Usuário não pode ser excluído: possui pessoa vinculada");
        }
        if (userCompanyRepository.existsByUserId(id)) {
            throw new ResourceInUseException("Usuário não pode ser excluído: possui vínculo(s) de empresa");
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UserResponseDTO toResponse(User entity) {
        UserResponseDTO response = objectMapper.convertValue(entity, UserResponseDTO.class);
        response.setAuthId(entity.getAuth_id());
        return response;
    }
}
