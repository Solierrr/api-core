package com.solaria.persistence.controller.identity;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.response.identity.SelfProfileResponseDTO;
import com.solaria.persistence.openapi.identity.SelfProfileOpenApi;
import com.solaria.persistence.service.identity.SelfProfileService;

@RestController
@RequestMapping("/api/users/me")
public class SelfProfileController implements SelfProfileOpenApi {

    private final SelfProfileService selfProfileService;

    public SelfProfileController(SelfProfileService selfProfileService) {
        this.selfProfileService = selfProfileService;
    }

    @Override
    @GetMapping
    public ResponseEntity<SelfProfileResponseDTO> getProfile() {
        return ResponseEntity.ok(selfProfileService.getProfile());
    }
}
