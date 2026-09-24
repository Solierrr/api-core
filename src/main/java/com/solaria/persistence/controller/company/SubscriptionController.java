package com.solaria.persistence.controller.company;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.request.company.SubscriptionRequestDTO;
import com.solaria.persistence.dto.response.company.SubscriptionResponseDTO;
import com.solaria.persistence.openapi.company.SubscriptionOpenApi;
import com.solaria.persistence.service.company.SubscriptionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController implements SubscriptionOpenApi {

    private final SubscriptionService subscriptionService;

    @Override
    @PostMapping
    public ResponseEntity<SubscriptionResponseDTO> save(@Valid @RequestBody SubscriptionRequestDTO dto) {
        SubscriptionResponseDTO response = subscriptionService.save(dto);
        return ResponseEntity.created(URI.create("/api/subscriptions/" + response.getId())).body(response);
    }

    @Override
    @PostMapping("/{id}/in-debt")
    public ResponseEntity<SubscriptionResponseDTO> markInDebt(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.markInDebt(id));
    }

    @Override
    @PostMapping("/{id}/suspension")
    public ResponseEntity<SubscriptionResponseDTO> suspend(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.suspend(id));
    }

    @Override
    @PostMapping("/{id}/reactivation")
    public ResponseEntity<SubscriptionResponseDTO> reactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.reactivate(id));
    }

    @Override
    @PostMapping("/{id}/end")
    public ResponseEntity<SubscriptionResponseDTO> end(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.end(id));
    }

    @Override
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<SubscriptionResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(subscriptionService.findById(id, companyId));
    }


    @Override
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<SubscriptionResponseDTO>> findBySupplier(@PathVariable UUID supplierId) {
        return ResponseEntity.ok(subscriptionService.findBySupplier(supplierId));
    }

    @Override
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<SubscriptionResponseDTO>> findAllByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(subscriptionService.findAllByCompany(companyId));
    }
}
