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

import com.solaria.persistence.dto.request.professional.ProfessionalRegistrationRequestDTO;
import com.solaria.persistence.dto.response.professional.ProfessionalRegistrationResponseDTO;
import com.solaria.persistence.openapi.professional.ProfessionalRegistrationOpenApi;
import com.solaria.persistence.service.professional.ProfessionalRegistrationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/professional-registrations")
@RequiredArgsConstructor
public class ProfessionalRegistrationController implements ProfessionalRegistrationOpenApi {

    private final ProfessionalRegistrationService professionalRegistrationService;

    @Override
    @PostMapping
    public ResponseEntity<ProfessionalRegistrationResponseDTO> save(
            @Valid @RequestBody ProfessionalRegistrationRequestDTO dto) {
        ProfessionalRegistrationResponseDTO response = professionalRegistrationService.save(dto);
        return ResponseEntity.created(URI.create("/api/professional-registrations/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ProfessionalRegistrationResponseDTO> update(@PathVariable UUID id,
            @Valid @RequestBody ProfessionalRegistrationRequestDTO dto) {
        return ResponseEntity.ok(professionalRegistrationService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        professionalRegistrationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalRegistrationResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(professionalRegistrationService.findById(id));
    }

    @Override
    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<List<ProfessionalRegistrationResponseDTO>> findByTechnician(@PathVariable UUID technicianId) {
        return ResponseEntity.ok(professionalRegistrationService.findByTechnician(technicianId));
    }
}
