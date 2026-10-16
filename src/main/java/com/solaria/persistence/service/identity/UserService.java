package com.solaria.persistence.service.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.dto.request.identity.UserRequestDTO;
import com.solaria.persistence.dto.request.identity.UpdateOwnUserRequestDTO;
import com.solaria.persistence.dto.response.identity.UserResponseDTO;
import com.solaria.persistence.dto.response.identity.UserSummaryResponseDTO;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.identity.PersonRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.repository.identity.UserPhotoRepository;
import com.solaria.persistence.repository.identity.UserRepository;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final UserPhotoRepository userPhotoRepository;
    private final ObjectMapper objectMapper;
    private final InternalUserProvisioningService provisioningService;

    public UserService(UserRepository userRepository,
                        PersonRepository personRepository,
                        UserCompanyRepository userCompanyRepository,
                        UserPhotoRepository userPhotoRepository,
                        ObjectMapper objectMapper,
                        InternalUserProvisioningService provisioningService) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.userPhotoRepository = userPhotoRepository;
        this.objectMapper = objectMapper;
        this.provisioningService = provisioningService;
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
        if (userPhotoRepository.existsByUserId(id)) {
            throw new ResourceInUseException("Usuário não pode ser excluído: possui foto(s) vinculada(s)");
        }
        userRepository.removeConnectionReferences(id);
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserSummaryResponseDTO findSummaryByAuthId(UUID authId) {
        User user = userRepository.findByAuthId(authId)
                .filter(candidate -> Boolean.TRUE.equals(candidate.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return new UserSummaryResponseDTO(user.getUsername(), user.getAvatar());
    }

    @Transactional
    public UserResponseDTO findMe(UUID authId) {
        return toResponse(findOrProvision(authId));
    }

    @Transactional
    public UserResponseDTO updateMe(UUID authId, UpdateOwnUserRequestDTO dto) {
        User user = findOrProvision(authId);
        if (userRepository.existsByUsernameAndIdNot(dto.username(), user.getId())) {
            throw new DuplicateResourceException("Nome de usuário indisponível");
        }
        user.setUsername(dto.username());
        return toResponse(userRepository.save(user));
    }

    private User findOrProvision(UUID authId) {
        return userRepository.findByAuthId(authId).orElseGet(() -> {
            provisioningService.provision(authId);
            return userRepository.findByAuthId(authId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));
        });
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
