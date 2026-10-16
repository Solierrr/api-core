package com.solaria.persistence.service.identity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.solaria.persistence.config.CloudinaryProperties;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.identity.UserPhoto;
import com.solaria.persistence.domain.enums.shared.PhotoType;
import com.solaria.persistence.dto.request.shared.PhotoCropRequestDTO;
import com.solaria.persistence.dto.response.identity.UserPhotoResponseDTO;
import com.solaria.persistence.dto.response.shared.PhotoCropDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.identity.UserPhotoRepository;
import com.solaria.persistence.repository.identity.UserRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import com.solaria.persistence.service.shared.CloudinaryService.UploadResult;
import com.solaria.persistence.service.shared.CloudinaryService;
import com.solaria.persistence.service.shared.PhotoCropValidator;
import com.solaria.persistence.service.shared.PhotoVariants;

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
        // imagem nova invalida o recorte anterior (as coordenadas pertenciam a outra imagem)
        clearCropFields(photo);
        if (photo.getCreatedAt() == null) {
            photo.setCreatedAt(OffsetDateTime.now());
        }
        // em caso de falha do BD, remove o asset do cloudnary
        try {
            UserPhotoResponseDTO response = toResponse(userPhotoRepository.save(photo));
            // o perfil le users.avatar/banner: mantem as colunas sincronizadas com a foto enviada
            syncProfileImage(user, type, result.url());
            return response;
        } catch (RuntimeException e) {
            cloudinaryService.delete(result.publicId());
            throw e;
        }
    }

    @Transactional
    public UserPhotoResponseDTO updateCrop(UUID userId, PhotoType type, PhotoCropRequestDTO crop) {
        rbac.requireOwnUser(userId);
        UserPhoto photo = findPhoto(userId, type);
        PhotoCropValidator.validate(type, crop, cloudinaryService.imageInfo(photo.getPublicId()));
        cloudinaryService.setCustomCoordinates(photo.getPublicId(), crop.x(), crop.y(), crop.width(), crop.height());
        photo.setCropX(crop.x());
        photo.setCropY(crop.y());
        photo.setCropWidth(crop.width());
        photo.setCropHeight(crop.height());
        return toResponse(userPhotoRepository.save(photo));
    }

    @Transactional
    public UserPhotoResponseDTO clearCrop(UUID userId, PhotoType type) {
        rbac.requireOwnUser(userId);
        UserPhoto photo = findPhoto(userId, type);
        clearCropFields(photo);
        return toResponse(userPhotoRepository.save(photo));
    }

    private UserPhoto findPhoto(UUID userId, PhotoType type) {
        return userPhotoRepository.findByUserIdAndType(userId, type)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Foto não encontrada para o usuário " + userId + " e tipo " + type));
    }

    private void clearCropFields(UserPhoto photo) {
        photo.setCropX(null);
        photo.setCropY(null);
        photo.setCropWidth(null);
        photo.setCropHeight(null);
    }

    @Transactional
    public void delete(UUID userId, PhotoType type) {
        rbac.requireOwnUser(userId);

        UserPhoto photo = userPhotoRepository.findByUserIdAndType(userId, type)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Foto não encontrada para o usuário " + userId + " e tipo " + type));

        cloudinaryService.delete(photo.getPublicId());
        syncProfileImage(photo.getUser(), type, null);
        userPhotoRepository.delete(photo);
    }

    @Transactional(readOnly = true)
    public List<UserPhotoResponseDTO> findByUser(UUID userId) {
        return userPhotoRepository.findByUserIdOrderByTypeAsc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void syncProfileImage(User user, PhotoType type, String url) {
        if (type == PhotoType.PROFILE) {
            user.setAvatar(url);
        } else {
            user.setBanner(url);
        }
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
        PhotoCropDTO crop = PhotoCropDTO.of(photo.getCropX(), photo.getCropY(), photo.getCropWidth(), photo.getCropHeight());
        response.setCrop(crop);
        response.setVariants(PhotoVariants.of(photo.getUrl(), photo.getType(), crop));
        return response;
    }
}
