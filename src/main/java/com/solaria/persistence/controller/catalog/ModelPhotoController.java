package com.solaria.persistence.controller.catalog;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.solaria.persistence.dto.response.catalog.ModelPhotoResponseDTO;
import com.solaria.persistence.openapi.catalog.ModelPhotoOpenApi;
import com.solaria.persistence.service.catalog.ModelPhotoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/model-photos")
@RequiredArgsConstructor
public class ModelPhotoController implements ModelPhotoOpenApi {

    private final ModelPhotoService modelPhotoService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ModelPhotoResponseDTO> upload(@RequestParam("modelId") UUID modelId,
                                                           @RequestParam("file") MultipartFile file) {
        ModelPhotoResponseDTO response = modelPhotoService.upload(modelId, file);
        return ResponseEntity.created(URI.create("/api/model-photos/" + response.getId())).body(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        modelPhotoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/model/{modelId}")
    public ResponseEntity<List<ModelPhotoResponseDTO>> findByModel(@PathVariable UUID modelId) {
        return ResponseEntity.ok(modelPhotoService.findByModel(modelId));
    }
}
