package com.solaria.persistence.service.execution;

import com.solaria.persistence.domain.entity.execution.ProjectPhoto;
import com.solaria.persistence.dto.response.execution.ProjectPhotoResponse;
import com.solaria.persistence.exception.*;
import com.solaria.persistence.repository.execution.ProjectPhotoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectPhotoService {
    private final ProjectService projects;
    private final ProjectAccess access;
    private final ProjectPhotoRepository photos;

    public List<ProjectPhotoResponse> list(UUID id) {
        access.read(projects.entity(id, false));
        return photos.metadata(id);
    }

    public ProjectPhotoResponse upload(UUID id, MultipartFile file, boolean cover) {
        var p = projects.entity(id, true);
        access.contribute(p);
        if (cover) access.manage(p);
        if (file.isEmpty() || file.getSize() > 8 * 1024 * 1024)
            throw new BusinessRuleException("Selecione uma imagem de até 8 MB");
        if (photos.countByProjectId(id) >= 30)
            throw new BusinessRuleException("Limite de 30 fotos por projeto");
        byte[] bytes;
        String mime;
        try {
            bytes = file.getBytes();
            try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                var readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext())
                    throw new BusinessRuleException("Imagem inválida; use JPG ou PNG");
                var reader = readers.next();
                try {
                    reader.setInput(input);
                    if ((long) reader.getWidth(0) * reader.getHeight(0) > 40000000)
                        throw new BusinessRuleException("Imagem muito grande");
                    String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                    mime =
                            switch (format) {
                                case "png" -> "image/png";
                                case "jpeg", "jpg" -> "image/jpeg";
                                default -> throw new BusinessRuleException("Use JPG ou PNG");
                            };
                } finally {
                    reader.dispose();
                }
            }
        } catch (IOException e) {
            throw new BusinessRuleException("Não foi possível ler a imagem");
        }
        if (cover) photos.clearCover(id);
        var photo = new ProjectPhoto();
        photo.setProjectId(id);
        photo.setName(
                file.getOriginalFilename() == null
                        ? "foto"
                        : file.getOriginalFilename()
                                .replaceAll("[\\/\r\n]", "_")
                                .substring(0, Math.min(160, file.getOriginalFilename().length())));
        photo.setMime(mime);
        photo.setContent(bytes);
        photo.setCover(cover);
        photo.setUploadedBy(access.user().getId());
        photos.save(photo);
        projects.record(p, "PHOTO_ADDED", "Foto adicionada");
        return response(photo);
    }

    public ProjectPhoto read(UUID projectId, UUID id) {
        access.read(projects.entity(projectId, false));
        return photo(projectId, id);
    }

    public void remove(UUID projectId, UUID id) {
        var p = projects.entity(projectId, true);
        access.manage(p);
        access.mutable(p);
        photos.delete(photo(projectId, id));
        projects.record(p, "PHOTO_REMOVED", "Foto removida");
    }

    public ProjectPhotoResponse cover(UUID projectId, UUID id) {
        var p = projects.entity(projectId, true);
        access.manage(p);
        access.mutable(p);
        photo(projectId, id);
        photos.clearCover(projectId);
        var target = photo(projectId, id);
        target.setCover(true);
        photos.save(target);
        projects.record(p, "COVER_CHANGED", "Capa atualizada");
        return response(target);
    }

    private ProjectPhoto photo(UUID projectId, UUID id) {
        return photos.findById(id)
                .filter(p -> projectId.equals(p.getProjectId()))
                .orElseThrow(() -> new ResourceNotFoundException("Foto indisponível"));
    }

    private ProjectPhotoResponse response(ProjectPhoto p) {
        return new ProjectPhotoResponse(
                p.getId(),
                p.getName(),
                p.getMime(),
                p.isCover(),
                p.getContent().length,
                p.getCreatedAt());
    }
}
