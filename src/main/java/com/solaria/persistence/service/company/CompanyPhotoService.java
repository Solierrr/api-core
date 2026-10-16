package com.solaria.persistence.service.company;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.solaria.persistence.config.CloudinaryProperties;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.company.CompanyPhoto;
import com.solaria.persistence.domain.enums.shared.PhotoType;
import com.solaria.persistence.dto.request.shared.PhotoCropRequestDTO;
import com.solaria.persistence.dto.response.company.CompanyPhotoResponseDTO;
import com.solaria.persistence.dto.response.shared.PhotoCropDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.company.CompanyPhotoRepository;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import com.solaria.persistence.service.shared.CloudinaryService.UploadResult;
import com.solaria.persistence.service.shared.CloudinaryService;
import com.solaria.persistence.service.shared.PhotoCropValidator;
import com.solaria.persistence.service.shared.PhotoVariants;

@Service
public class CompanyPhotoService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private final CompanyPhotoRepository companyPhotoRepository;
    private final CompanyRepository companyRepository;
    private final CloudinaryService cloudinaryService;
    private final CloudinaryProperties cloudinaryProperties;
    private final RbacAuthorizationService rbac;

    public CompanyPhotoService(CompanyPhotoRepository companyPhotoRepository,
                               CompanyRepository companyRepository,
                               CloudinaryService cloudinaryService,
                               CloudinaryProperties cloudinaryProperties,
                               RbacAuthorizationService rbac) {
        this.companyPhotoRepository = companyPhotoRepository;
        this.companyRepository = companyRepository;
        this.cloudinaryService = cloudinaryService;
        this.cloudinaryProperties = cloudinaryProperties;
        this.rbac = rbac;
    }

    @Transactional
    public CompanyPhotoResponseDTO upload(UUID companyId, PhotoType type, MultipartFile file) {
        rbac.requireOwnCompany(companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + companyId));


        UploadResult result = cloudinaryService.upload(file, folder(companyId), publicId(companyId, type), true,
                ALLOWED_CONTENT_TYPES, MAX_BYTES);

        CompanyPhoto photo = companyPhotoRepository.findByCompanyIdAndType(companyId, type)
                .orElseGet(CompanyPhoto::new);
        photo.setCompany(company);
        photo.setType(type);
        photo.setUrl(result.url());
        photo.setPublicId(result.publicId());
        // imagem nova invalida o recorte anterior (as coordenadas pertenciam a outra imagem)
        clearCropFields(photo);
        if (photo.getCreatedAt() == null) {
            photo.setCreatedAt(OffsetDateTime.now());
        }

        // se a foto por algum motivo não salva no BD, deleta ela no cloudnary
        try {
            return toResponse(companyPhotoRepository.save(photo));
        } catch (RuntimeException e) {
            cloudinaryService.delete(result.publicId());
            throw e;
        }
    }

    @Transactional
    public CompanyPhotoResponseDTO updateCrop(UUID companyId, PhotoType type, PhotoCropRequestDTO crop) {
        rbac.requireOwnCompany(companyId);
        CompanyPhoto photo = findPhoto(companyId, type);
        PhotoCropValidator.validate(type, crop, cloudinaryService.imageInfo(photo.getPublicId()));
        cloudinaryService.setCustomCoordinates(photo.getPublicId(), crop.x(), crop.y(), crop.width(), crop.height());
        photo.setCropX(crop.x());
        photo.setCropY(crop.y());
        photo.setCropWidth(crop.width());
        photo.setCropHeight(crop.height());
        return toResponse(companyPhotoRepository.save(photo));
    }

    @Transactional
    public CompanyPhotoResponseDTO clearCrop(UUID companyId, PhotoType type) {
        rbac.requireOwnCompany(companyId);
        CompanyPhoto photo = findPhoto(companyId, type);
        clearCropFields(photo);
        return toResponse(companyPhotoRepository.save(photo));
    }

    private CompanyPhoto findPhoto(UUID companyId, PhotoType type) {
        return companyPhotoRepository.findByCompanyIdAndType(companyId, type)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Foto não encontrada para a empresa " + companyId + " e tipo " + type));
    }

    private void clearCropFields(CompanyPhoto photo) {
        photo.setCropX(null);
        photo.setCropY(null);
        photo.setCropWidth(null);
        photo.setCropHeight(null);
    }

    @Transactional
    public void delete(UUID companyId, PhotoType type) {
        rbac.requireOwnCompany(companyId);

        CompanyPhoto photo = companyPhotoRepository.findByCompanyIdAndType(companyId, type)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Foto não encontrada para a empresa " + companyId + " e tipo " + type));

        cloudinaryService.delete(photo.getPublicId());
        companyPhotoRepository.delete(photo);
    }

    @Transactional(readOnly = true)
    public List<CompanyPhotoResponseDTO> findByCompany(UUID companyId) {
        return companyPhotoRepository.findByCompanyIdOrderByTypeAsc(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    private String folder(UUID companyId) {
        return cloudinaryProperties.getFolderPrefix() + "/empresas/" + companyId;
    }

    private String publicId(UUID companyId, PhotoType type) {
        return folder(companyId) + "/" + type.name().toLowerCase();
    }

    private CompanyPhotoResponseDTO toResponse(CompanyPhoto photo) {
        CompanyPhotoResponseDTO response = new CompanyPhotoResponseDTO();
        response.setId(photo.getId());
        response.setCompanyId(photo.getCompany().getId());
        response.setType(photo.getType());
        response.setUrl(photo.getUrl());
        response.setCreatedAt(photo.getCreatedAt());
        PhotoCropDTO crop = PhotoCropDTO.of(photo.getCropX(), photo.getCropY(), photo.getCropWidth(), photo.getCropHeight());
        response.setCrop(crop);
        response.setVariants(PhotoVariants.of(photo.getUrl(), photo.getType(), crop));
        return response;
    }
}
