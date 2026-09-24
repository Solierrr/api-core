package com.solaria.persistence.controller.company;

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

import com.solaria.persistence.dto.request.company.BusinessContactRequestDTO;
import com.solaria.persistence.dto.response.company.BusinessContactResponseDTO;
import com.solaria.persistence.openapi.company.BusinessContactOpenApi;
import com.solaria.persistence.service.company.BusinessContactService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/business-contacts")
@RequiredArgsConstructor
public class BusinessContactController implements BusinessContactOpenApi {

    private final BusinessContactService businessContactService;

    @Override
    @PostMapping
    public ResponseEntity<BusinessContactResponseDTO> save(@Valid @RequestBody BusinessContactRequestDTO dto) {
        BusinessContactResponseDTO response = businessContactService.save(dto);
        return ResponseEntity.created(URI.create("/api/business-contacts/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<BusinessContactResponseDTO> update(@PathVariable UUID id,
                                                               @Valid @RequestBody BusinessContactRequestDTO dto) {
        return ResponseEntity.ok(businessContactService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        businessContactService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<BusinessContactResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(businessContactService.findById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<BusinessContactResponseDTO>> findAll() {
        return ResponseEntity.ok(businessContactService.findAll());
    }
}
