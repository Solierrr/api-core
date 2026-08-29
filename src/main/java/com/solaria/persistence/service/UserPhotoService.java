package com.solaria.persistence.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.solaria.persistence.config.CloudinaryProperties;
import com.solaria.persistence.domain.entity.User;
import com.solaria.persistence.domain.entity.UserPhoto;
import com.solaria.persistence.domain.enums.PhotoType;
import com.solaria.persistence.dto.response.UserPhotoResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.UserPhotoRepository;
import com.solaria.persistence.repository.UserRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import com.solaria.persistence.service.CloudinaryService.UploadResult;

@Service
public class UserPhotoService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private final UserPhotoRepository userPhotoRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final CloudinaryProperties cloudinaryProperties;
    private final RbacAuthorizationService rbac;

    public UserPhotoService(UserPhotoRepository userPhotoRepository,
                            UserRepository userRepository,
                            CloudinaryService cloudinaryService,
                            CloudinaryProperties cloudinaryProperties,
                            RbacAuthorizationService rbac) {
        this.userPhotoRepository = userPhotoRepository;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
        this.cloudinaryProperties = cloudinaryProperties;
        this.rbac = rbac;
    }

    @Transactional
    public UserPhotoResponseDTO upload(UUID userId, PhotoType type, MultipartFile file) {
        rbac.requireOwnUser(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + userId));


        UploadResult result = cloudinaryService.upload(file, folder(userId), publicId(userId, type), true,
                ALLOWED_CONTENT_TYPES, MAX_BYTES);

        UserPhoto photo = userPhotoRepository.findByUserIdAndType(userId, type).orElseGet(UserPhoto::new);
        photo.setUser(user);
        photo.setType(type);
        photo.setUrl(result.url());
        photo.setPublicId(result.publicId());
        if (photo.getCreatedAt() == null) {
            photo.setCreatedAt(OffsetDateTime.now());
        }
        // em caso de falha do BD, remove o asset do cloudnary
        try {
            return toResponse(userPhotoRepository.save(photo));
        } catch (RuntimeException e) {
            cloudinaryService.delete(result.publicId());
            throw e;
        }
    }

    @Transactional
    public void delete(UUID userId, PhotoType type) {
        rbac.requireOwnUser(userId);

        UserPhoto photo = userPhotoRepository.findByUserIdAndType(userId, type)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Foto não encontrada para o usuário " + userId + " e tipo " + type));

        cloudinaryService.delete(photo.getPublicId());
        userPhotoRepository.delete(photo);
    }

    @Transactional(readOnly = true)
    public List<UserPhotoResponseDTO> findByUser(UUID userId) {
        return userPhotoRepository.findByUserIdOrderByTypeAsc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private String folder(UUID userId) {
        return cloudinaryProperties.getFolderPrefix() + "/usuarios/" + userId;
    }

    private String publicId(UUID userId, PhotoType type) {
        return folder(userId) + "/" + type.name().toLowerCase();
    }

    private UserPhotoResponseDTO toResponse(UserPhoto photo) {
        UserPhotoResponseDTO response = new UserPhotoResponseDTO();
        response.setId(photo.getId());
        response.setUserId(photo.getUser().getId());
        response.setType(photo.getType());
        response.setUrl(photo.getUrl());
        response.setCreatedAt(photo.getCreatedAt());
        return response;
    }
}
