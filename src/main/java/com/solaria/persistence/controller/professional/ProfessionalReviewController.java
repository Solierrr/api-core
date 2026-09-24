package com.solaria.persistence.controller.professional;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.request.professional.ProfessionalReviewRequestDTO;
import com.solaria.persistence.dto.response.professional.ProfessionalReviewResponseDTO;
import com.solaria.persistence.openapi.professional.ProfessionalReviewOpenApi;
import com.solaria.persistence.service.professional.ProfessionalReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/professional-reviews")
@RequiredArgsConstructor
public class ProfessionalReviewController implements ProfessionalReviewOpenApi {

    private final ProfessionalReviewService professionalReviewService;

    @Override
    @PostMapping
    public ResponseEntity<ProfessionalReviewResponseDTO> save(
            @Valid @RequestBody ProfessionalReviewRequestDTO dto) {
        ProfessionalReviewResponseDTO response = professionalReviewService.save(dto);
        return ResponseEntity.created(URI.create("/api/professional-reviews/" + response.getId())).body(response);
    }

    @Override
    @PatchMapping("/{id}/deactivation")
    public ResponseEntity<ProfessionalReviewResponseDTO> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(professionalReviewService.deactivate(id));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalReviewResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(professionalReviewService.findById(id));
    }

    @Override
    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<ProfessionalReviewResponseDTO>> findByProfessional(
            @PathVariable UUID professionalId) {
        return ResponseEntity.ok(professionalReviewService.findByProfessional(professionalId));
    }
}
