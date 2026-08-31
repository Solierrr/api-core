package com.solaria.persistence.controller.professional;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.request.professional.TechnicalCourseRequestDTO;
import com.solaria.persistence.dto.response.professional.TechnicalCourseResponseDTO;
import com.solaria.persistence.openapi.professional.TechnicalCourseOpenApi;
import com.solaria.persistence.service.professional.TechnicalCourseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/technical-courses")
@RequiredArgsConstructor
public class TechnicalCourseController implements TechnicalCourseOpenApi {

    private final TechnicalCourseService technicalCourseService;

    @Override
    @PostMapping
    public ResponseEntity<TechnicalCourseResponseDTO> save(@Valid @RequestBody TechnicalCourseRequestDTO dto) {
        TechnicalCourseResponseDTO response = technicalCourseService.save(dto);
        return ResponseEntity.created(URI.create("/api/technical-courses/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<TechnicalCourseResponseDTO> update(@PathVariable UUID id,
                                                                @Valid @RequestBody TechnicalCourseRequestDTO dto) {
        return ResponseEntity.ok(technicalCourseService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        technicalCourseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<TechnicalCourseResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(technicalCourseService.findById(id, companyId));
    }

    @Override
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<TechnicalCourseResponseDTO>> findByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(technicalCourseService.findByCompany(companyId));
    }
}
