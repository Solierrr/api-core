package com.solaria.persistence.controller.catalog;

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

import com.solaria.persistence.dto.request.catalog.OfferRequestDTO;
import com.solaria.persistence.dto.response.catalog.OfferResponseDTO;
import com.solaria.persistence.openapi.catalog.OfferOpenApi;
import com.solaria.persistence.service.catalog.OfferService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController implements OfferOpenApi {

    private final OfferService offerService;

    @Override
    @PostMapping
    public ResponseEntity<OfferResponseDTO> save(@Valid @RequestBody OfferRequestDTO dto) {
        OfferResponseDTO response = offerService.save(dto);
        return ResponseEntity.created(URI.create("/api/offers/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<OfferResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody OfferRequestDTO dto) {
        return ResponseEntity.ok(offerService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        offerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<OfferResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(offerService.findById(id, companyId));
    }

    @Override
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<OfferResponseDTO>> findBySupplier(@PathVariable UUID supplierId) {
        return ResponseEntity.ok(offerService.findBySupplier(supplierId));
    }

    @Override
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<OfferResponseDTO>> findAllByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(offerService.findAllByCompany(companyId));
    }

    @Override
    @GetMapping("/catalog")
    public ResponseEntity<List<OfferResponseDTO>> findCatalog() {
        return ResponseEntity.ok(offerService.findCatalog());
    }
}
