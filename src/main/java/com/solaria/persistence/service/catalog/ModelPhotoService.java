package com.solaria.persistence.service.catalog;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.solaria.persistence.config.CloudinaryProperties;
import com.solaria.persistence.domain.entity.catalog.Model;
import com.solaria.persistence.domain.entity.catalog.ModelPhoto;
import com.solaria.persistence.dto.response.catalog.ModelPhotoResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.catalog.ModelPhotoRepository;
import com.solaria.persistence.repository.catalog.ModelRepository;
import com.solaria.persistence.service.shared.CloudinaryService.UploadResult;
import com.solaria.persistence.service.shared.CloudinaryService;

@Service
public class ModelPhotoService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_BYTES = 8L * 1024 * 1024;

    private final ModelPhotoRepository modelPhotoRepository;
    private final ModelRepository modelRepository;
    private final CloudinaryService cloudinaryService;
    private final CloudinaryProperties cloudinaryProperties;
    private final com.solaria.persistence.security.rbac.RbacAuthorizationService rbac;

    public ModelPhotoService(ModelPhotoRepository modelPhotoRepository,
                             ModelRepository modelRepository,
                             CloudinaryService cloudinaryService,
                             CloudinaryProperties cloudinaryProperties,
                             com.solaria.persistence.security.rbac.RbacAuthorizationService rbac) {
        this.modelPhotoRepository = modelPhotoRepository;
        this.modelRepository = modelRepository;
        this.cloudinaryService = cloudinaryService;
        this.cloudinaryProperties = cloudinaryProperties;
        this.rbac = rbac;
    }


    @Transactional
    public ModelPhotoResponseDTO upload(UUID modelId, MultipartFile file) {
        return upload(modelId, file, false);
    }

    @Transactional
    public ModelPhotoResponseDTO upload(UUID modelId, MultipartFile file, boolean primary) {
        Model model = modelRepository.lockById(modelId)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo não encontrado(a) com ID: " + modelId));

        requireMutable(model);

        UploadResult result = cloudinaryService.upload(file, folder(modelId), null, false,
                ALLOWED_CONTENT_TYPES, MAX_BYTES);

        cloudinaryService.cleanupOnRollback(result.publicId(), false);
        ModelPhoto photo = new ModelPhoto();
        photo.setModel(model);
        photo.setUrl(result.url());
        photo.setPublicId(result.publicId());
        photo.setCreatedAt(OffsetDateTime.now());
        photo.setPrimary(primary);

        // em caso de falha do BD, remove o asset do cloudnary
        try {
            if (primary) {
                var previous = modelPhotoRepository.findByModelIdAndPrimaryTrue(modelId);
                previous.forEach(existing -> existing.setPrimary(false));
                modelPhotoRepository.saveAllAndFlush(previous);
            }
            return toResponse(modelPhotoRepository.saveAndFlush(photo));
        } catch (RuntimeException e) {

            try {
                cloudinaryService.delete(result.publicId());
            } catch (RuntimeException cleanupError) {
                e.addSuppressed(cleanupError);
            }
            throw e;
        }
    }

    @Transactional
    public void delete(UUID id) {
        ModelPhoto photo = modelPhotoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Foto não encontrada com ID: " + id));

        requireMutable(modelRepository.lockById(photo.getModel().getId()).orElseThrow(
                () -> new ResourceNotFoundException("Model not found")));
        modelPhotoRepository.delete(photo);
        cloudinaryService.deleteAfterCommit(photo.getPublicId());
    }

    @Transactional(readOnly = true)
    public List<ModelPhotoResponseDTO> findByModel(UUID modelId) {
        return modelPhotoRepository.findByModelIdOrderByCreatedAtDesc(modelId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void requireMutable(Model model) {
        if (model.getCreatorCompanyId() == null) {
            throw new com.solaria.persistence.exception.UnauthorizedAccessException("Model has no registered owner");
        }
        rbac.requireOwnCompany(model.getCreatorCompanyId());
        if (model.getStatus() != com.solaria.persistence.domain.enums.catalog.ModelStatus.UNDER_ANALYSIS) {
            throw new com.solaria.persistence.exception.BusinessRuleException("Only models under analysis allow photo changes");
        }
    }

    private String folder(UUID modelId) {
        return cloudinaryProperties.getFolderPrefix() + "/placas-solares/" + modelId;
    }

    private ModelPhotoResponseDTO toResponse(ModelPhoto photo) {
        ModelPhotoResponseDTO response = new ModelPhotoResponseDTO();
        response.setId(photo.getId());
        response.setModelId(photo.getModel().getId());
        response.setUrl(photo.getUrl());
        response.setPrimary(photo.isPrimary());
        response.setCreatedAt(photo.getCreatedAt());
        return response;
    }
}
