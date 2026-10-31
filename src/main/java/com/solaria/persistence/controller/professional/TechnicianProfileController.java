package com.solaria.persistence.controller.professional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.request.professional.TechnicianProfileRequest;
import com.solaria.persistence.dto.response.professional.TechnicianProfileResponse;
import com.solaria.persistence.openapi.professional.TechnicianProfileOpenApi;
import com.solaria.persistence.service.professional.TechnicianProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/technicians/me/profile")
@RequiredArgsConstructor
public class TechnicianProfileController implements TechnicianProfileOpenApi {

    private final TechnicianProfileService service;

    @Override
    @GetMapping
    public ResponseEntity<TechnicianProfileResponse> get() {
        return ResponseEntity.ok(service.get());
    }

    @Override
    @PutMapping
    public ResponseEntity<TechnicianProfileResponse> save(@Valid @RequestBody TechnicianProfileRequest request) {
        return ResponseEntity.ok(service.save(request));
    }

    @Override
    @PostMapping("/completion")
    public ResponseEntity<TechnicianProfileResponse> complete() {
        return ResponseEntity.ok(service.complete());
    }
}
