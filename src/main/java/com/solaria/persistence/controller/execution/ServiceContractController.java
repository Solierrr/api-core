package com.solaria.persistence.controller.execution;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.request.execution.ServiceContractRequestDTO;
import com.solaria.persistence.dto.response.execution.ServiceContractResponseDTO;
import com.solaria.persistence.openapi.execution.ServiceContractOpenApi;
import com.solaria.persistence.service.execution.ServiceContractService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/service-contracts")
@RequiredArgsConstructor
public class ServiceContractController implements ServiceContractOpenApi {

    private final ServiceContractService serviceContractService;

    @Override
    @PostMapping
    public ResponseEntity<ServiceContractResponseDTO> save(@Valid @RequestBody ServiceContractRequestDTO dto) {
        ServiceContractResponseDTO response = serviceContractService.save(dto);
        return ResponseEntity.created(URI.create("/api/service-contracts/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ServiceContractResponseDTO> update(@PathVariable UUID id,
                                                               @Valid @RequestBody ServiceContractRequestDTO dto) {
        return ResponseEntity.ok(serviceContractService.update(id, dto));
    }

    @Override
    @PatchMapping("/{id}/utility-approval")
    public ResponseEntity<ServiceContractResponseDTO> markUtilityApproved(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceContractService.markUtilityApproved(id));
    }

    @Override
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<ServiceContractResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(serviceContractService.findById(id, companyId));
    }

    @Override
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<ServiceContractResponseDTO> findByService(@PathVariable UUID serviceId) {
        return ResponseEntity.ok(serviceContractService.findByService(serviceId));
    }
}
