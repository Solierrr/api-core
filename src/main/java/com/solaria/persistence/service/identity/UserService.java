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
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.identity.PersonRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.repository.identity.UserPhotoRepository;
import com.solaria.persistence.repository.identity.UserRepository;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final UserPhotoRepository userPhotoRepository;
    private final ObjectMapper objectMapper;
    private final CurrentUserService currentUserService;
    private final RbacAuthorizationService rbac;

    public UserService(UserRepository userRepository,
                        PersonRepository personRepository,
                        UserCompanyRepository userCompanyRepository,
                        UserPhotoRepository userPhotoRepository,
                        ObjectMapper objectMapper,
                        CurrentUserService currentUserService,
                        RbacAuthorizationService rbac) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.userPhotoRepository = userPhotoRepository;
        this.objectMapper = objectMapper;
        this.currentUserService = currentUserService;
        this.rbac = rbac;
    }

    @Transactional
    public UserResponseDTO updateMe(UpdateOwnUserRequestDTO dto) {
        User user = currentUserService.getProvisionedCurrentUser();
        if (userRepository.existsByUsernameAndIdNot(dto.username(), user.getId())) {
            throw new DuplicateResourceException("Nome de usuário indisponível");
        }
        user.setUsername(dto.username());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponseDTO update(UUID id, UserRequestDTO dto) {
        currentUserService.getProvisionedCurrentUser();
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Usuário com id:" + id + " não encontrado para atualização"));
        rbac.requireOwnUser(user.getId());
        if (!user.getAuthId().equals(dto.getAuthId())) {
            throw new InvalidFieldException("ID de autenticação (authId) imutável: " + dto.getAuthId());
        }

        user.setUsername(dto.getUsername());
        user.setAvatar(dto.getAvatar());
        user.setBanner(dto.getBanner());

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteById(UUID id) {
        currentUserService.getProvisionedCurrentUser();
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário com id:" + id + " não encontrado para exclusão");
        }
        rbac.requireOwnUser(id);
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
    public UserSummaryResponseDTO findSummaryByAuthId(UUID authId) {
        User user = userRepository.findByAuthId(authId)
                .filter(candidate -> Boolean.TRUE.equals(candidate.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return new UserSummaryResponseDTO(user.getUsername(), user.getAvatar());
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
        response.setAuthId(entity.getAuthId());
        return response;
    }
}
