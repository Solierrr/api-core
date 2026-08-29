package com.solaria.persistence.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.solaria.persistence.config.CloudinaryProperties;
import com.solaria.persistence.domain.entity.LocalUnit;
import com.solaria.persistence.domain.entity.LocalUnitPhoto;
import com.solaria.persistence.dto.response.LocalUnitPhotoResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.LocalUnitPhotoRepository;
import com.solaria.persistence.repository.LocalUnitRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import com.solaria.persistence.service.CloudinaryService.UploadResult;

@Service
public class LocalUnitPhotoService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_BYTES = 8L * 1024 * 1024;

    private final LocalUnitPhotoRepository localUnitPhotoRepository;
    private final LocalUnitRepository localUnitRepository;
    private final CloudinaryService cloudinaryService;
    private final CloudinaryProperties cloudinaryProperties;
    private final RbacAuthorizationService rbac;

    public LocalUnitPhotoService(LocalUnitPhotoRepository localUnitPhotoRepository,
                                 LocalUnitRepository localUnitRepository,
                                 CloudinaryService cloudinaryService,
                                 CloudinaryProperties cloudinaryProperties,
                                 RbacAuthorizationService rbac) {
        this.localUnitPhotoRepository = localUnitPhotoRepository;
        this.localUnitRepository = localUnitRepository;
        this.cloudinaryService = cloudinaryService;
        this.cloudinaryProperties = cloudinaryProperties;
        this.rbac = rbac;
    }

    @Transactional
    public LocalUnitPhotoResponseDTO upload(UUID localUnitId, MultipartFile file) {
        LocalUnit localUnit = localUnitRepository.findById(localUnitId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade Local não encontrada com ID: " + localUnitId));

        rbac.requireOwnCompany(localUnit.getRequester().getCompany().getId());


        UploadResult result = cloudinaryService.upload(file, folder(localUnitId), null, false,
                ALLOWED_CONTENT_TYPES, MAX_BYTES);

        LocalUnitPhoto photo = new LocalUnitPhoto();
        photo.setLocalUnit(localUnit);
        photo.setUrl(result.url());
        photo.setPublicId(result.publicId());
        photo.setCreatedAt(OffsetDateTime.now());

        // em caso de falha do BD, remove o asset do cloudnary
        try {
            return toResponse(localUnitPhotoRepository.save(photo));
        } catch (RuntimeException e) {
            cloudinaryService.delete(result.publicId());
            throw e;
        }
    }

    @Transactional
    public void delete(UUID id) {
        LocalUnitPhoto photo = localUnitPhotoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Foto não encontrada com ID: " + id));

        rbac.requireOwnCompany(photo.getLocalUnit().getRequester().getCompany().getId());


        cloudinaryService.delete(photo.getPublicId());
        localUnitPhotoRepository.delete(photo);
    }

    @Transactional(readOnly = true)
    public List<LocalUnitPhotoResponseDTO> findByLocalUnit(UUID localUnitId) {
        return localUnitPhotoRepository.findByLocalUnitIdOrderByCreatedAtDesc(localUnitId).stream()
                .map(this::toResponse)
                .toList();
    }

    private String folder(UUID localUnitId) {
        return cloudinaryProperties.getFolderPrefix() + "/unidades-locais/" + localUnitId;
    }

    private LocalUnitPhotoResponseDTO toResponse(LocalUnitPhoto photo) {
        LocalUnitPhotoResponseDTO response = new LocalUnitPhotoResponseDTO();
        response.setId(photo.getId());
        response.setLocalUnitId(photo.getLocalUnit().getId());
        response.setUrl(photo.getUrl());
        response.setCreatedAt(photo.getCreatedAt());
        return response;
    }
}
