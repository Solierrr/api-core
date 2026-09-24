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

    public ModelPhotoService(ModelPhotoRepository modelPhotoRepository,
                             ModelRepository modelRepository,
                             CloudinaryService cloudinaryService,
                             CloudinaryProperties cloudinaryProperties) {
        this.modelPhotoRepository = modelPhotoRepository;
        this.modelRepository = modelRepository;
        this.cloudinaryService = cloudinaryService;
        this.cloudinaryProperties = cloudinaryProperties;
    }


    @Transactional
    public ModelPhotoResponseDTO upload(UUID modelId, MultipartFile file) {
        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo não encontrado(a) com ID: " + modelId));


        UploadResult result = cloudinaryService.upload(file, folder(modelId), null, false,
                ALLOWED_CONTENT_TYPES, MAX_BYTES);

        ModelPhoto photo = new ModelPhoto();
        photo.setModel(model);
        photo.setUrl(result.url());
        photo.setPublicId(result.publicId());
        photo.setCreatedAt(OffsetDateTime.now());

        // em caso de falha do BD, remove o asset do cloudnary
        try {
            return toResponse(modelPhotoRepository.save(photo));
        } catch (RuntimeException e) {

            cloudinaryService.delete(result.publicId());
            throw e;
        }
    }

    @Transactional
    public void delete(UUID id) {
        ModelPhoto photo = modelPhotoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Foto não encontrada com ID: " + id));

        cloudinaryService.delete(photo.getPublicId());
        modelPhotoRepository.delete(photo);
    }

    @Transactional(readOnly = true)
    public List<ModelPhotoResponseDTO> findByModel(UUID modelId) {
        return modelPhotoRepository.findByModelIdOrderByCreatedAtDesc(modelId).stream()
                .map(this::toResponse)
                .toList();
    }

    private String folder(UUID modelId) {
        return cloudinaryProperties.getFolderPrefix() + "/placas-solares/" + modelId;
    }

    private ModelPhotoResponseDTO toResponse(ModelPhoto photo) {
        ModelPhotoResponseDTO response = new ModelPhotoResponseDTO();
        response.setId(photo.getId());
        response.setModelId(photo.getModel().getId());
        response.setUrl(photo.getUrl());
        response.setCreatedAt(photo.getCreatedAt());
        return response;
    }
}
