package com.solaria.persistence.controller.company;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.domain.enums.company.PaymentMethod;
import com.solaria.persistence.dto.request.company.ChargeRequestDTO;
import com.solaria.persistence.dto.response.company.ChargeResponseDTO;
import com.solaria.persistence.openapi.company.ChargeOpenApi;
import com.solaria.persistence.service.company.ChargeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/charges")
@RequiredArgsConstructor
public class ChargeController implements ChargeOpenApi {

    private final ChargeService chargeService;

    @Override
    @PostMapping
    public ResponseEntity<ChargeResponseDTO> save(@Valid @RequestBody ChargeRequestDTO dto) {
        ChargeResponseDTO response = chargeService.save(dto);
        return ResponseEntity.created(URI.create("/api/charges/" + response.getId())).body(response);
    }

    @Override
    @PostMapping("/{id}/payment")
    public ResponseEntity<ChargeResponseDTO> pay(@PathVariable UUID id) {
        return ResponseEntity.ok(chargeService.pay(id));
    }

    @Override
    @PostMapping("/{id}/cancellation")
    public ResponseEntity<ChargeResponseDTO> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(chargeService.cancel(id));
    }

    @Override
    @PostMapping("/{id}/refund")
    public ResponseEntity<ChargeResponseDTO> refund(@PathVariable UUID id) {
        return ResponseEntity.ok(chargeService.refund(id));
    }

    @Override
    @PostMapping("/subscription/{subscriptionId}/generate")
    public ResponseEntity<Void> generateFromSubscription(
            @PathVariable UUID subscriptionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestParam(defaultValue = "BOLETO") PaymentMethod paymentMethod) {
        chargeService.generateFromSubscription(subscriptionId, dueDate, paymentMethod);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<ChargeResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(chargeService.findById(id, companyId));
    }

    @Override
    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<ChargeResponseDTO>> findBySubscription(@PathVariable UUID subscriptionId) {
        return ResponseEntity.ok(chargeService.findBySubscription(subscriptionId));
    }

    @Override
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<ChargeResponseDTO>> findAllByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(chargeService.findAllByCompany(companyId));
    }
}
