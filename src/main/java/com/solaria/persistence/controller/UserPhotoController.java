package com.solaria.persistence.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.solaria.persistence.domain.enums.PhotoType;
import com.solaria.persistence.dto.response.UserPhotoResponseDTO;
import com.solaria.persistence.openapi.UserPhotoOpenApi;
import com.solaria.persistence.service.UserPhotoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user-photos")
@RequiredArgsConstructor
public class UserPhotoController implements UserPhotoOpenApi {

    private final UserPhotoService userPhotoService;

    @Override
    @PutMapping(value = "/user/{userId}/type/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserPhotoResponseDTO> upload(@PathVariable UUID userId,
                                                          @PathVariable PhotoType type,
                                                          @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userPhotoService.upload(userId, type, file));
    }

    @Override
    @DeleteMapping("/user/{userId}/type/{type}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId, @PathVariable PhotoType type) {
        userPhotoService.delete(userId, type);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserPhotoResponseDTO>> findByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userPhotoService.findByUser(userId));
    }
}
