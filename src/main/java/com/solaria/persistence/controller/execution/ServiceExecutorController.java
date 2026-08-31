package com.solaria.persistence.controller.execution;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.patch.execution.UpdateFunctionDTO;
import com.solaria.persistence.dto.request.execution.ServiceExecutorRequestDTO;
import com.solaria.persistence.dto.response.execution.ServiceExecutorResponseDTO;
import com.solaria.persistence.openapi.execution.ServiceExecutorOpenApi;
import com.solaria.persistence.service.execution.ServiceExecutorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/service-executors")
@RequiredArgsConstructor
public class ServiceExecutorController implements ServiceExecutorOpenApi {

    private final ServiceExecutorService serviceExecutorService;

    @Override
    @PostMapping
    public ResponseEntity<ServiceExecutorResponseDTO> save(@Valid @RequestBody ServiceExecutorRequestDTO dto) {
        ServiceExecutorResponseDTO response = serviceExecutorService.save(dto);
        return ResponseEntity.status(201).body(response);
    }

    @Override
    @PatchMapping("/{id}/function")
    public ResponseEntity<ServiceExecutorResponseDTO> updateFunction(@PathVariable UUID id,
                                                                       @Valid @RequestBody UpdateFunctionDTO dto) {
        return ResponseEntity.ok(serviceExecutorService.updateFunction(id, dto.getFunction()));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        serviceExecutorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<ServiceExecutorResponseDTO>> findByService(@PathVariable UUID serviceId) {
        return ResponseEntity.ok(serviceExecutorService.findByService(serviceId));
    }
}
