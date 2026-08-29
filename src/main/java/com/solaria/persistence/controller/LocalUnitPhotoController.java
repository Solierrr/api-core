package com.solaria.persistence.controller;

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

import com.solaria.persistence.dto.response.LocalUnitPhotoResponseDTO;
import com.solaria.persistence.openapi.LocalUnitPhotoOpenApi;
import com.solaria.persistence.service.LocalUnitPhotoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/local-unit-photos")
@RequiredArgsConstructor
public class LocalUnitPhotoController implements LocalUnitPhotoOpenApi {

    private final LocalUnitPhotoService localUnitPhotoService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LocalUnitPhotoResponseDTO> upload(@RequestParam("localUnitId") UUID localUnitId,
                                                               @RequestParam("file") MultipartFile file) {
        LocalUnitPhotoResponseDTO response = localUnitPhotoService.upload(localUnitId, file);
        return ResponseEntity.created(URI.create("/api/local-unit-photos/" + response.getId())).body(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        localUnitPhotoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/local-unit/{localUnitId}")
    public ResponseEntity<List<LocalUnitPhotoResponseDTO>> findByLocalUnit(@PathVariable UUID localUnitId) {
        return ResponseEntity.ok(localUnitPhotoService.findByLocalUnit(localUnitId));
    }
}
