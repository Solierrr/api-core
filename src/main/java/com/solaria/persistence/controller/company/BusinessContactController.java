package com.solaria.persistence.controller.company;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.response.company.BusinessContactResponseDTO;
import com.solaria.persistence.openapi.company.BusinessContactOpenApi;
import com.solaria.persistence.service.company.BusinessContactService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/business-contacts")
@RequiredArgsConstructor
public class BusinessContactController implements BusinessContactOpenApi {

    private final BusinessContactService businessContactService;

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
