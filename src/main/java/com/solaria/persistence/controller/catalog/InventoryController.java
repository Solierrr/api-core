package com.solaria.persistence.controller.catalog;

import java.net.URI;
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

import com.solaria.persistence.dto.patch.catalog.UpdateQuantityDTO;
import com.solaria.persistence.dto.request.catalog.InventoryRequestDTO;
import com.solaria.persistence.dto.response.catalog.InventoryResponseDTO;
import com.solaria.persistence.openapi.catalog.InventoryOpenApi;
import com.solaria.persistence.service.catalog.InventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController implements InventoryOpenApi {

    private final InventoryService inventoryService;

    @Override
    @PostMapping
    public ResponseEntity<InventoryResponseDTO> save(@Valid @RequestBody InventoryRequestDTO dto) {
        InventoryResponseDTO response = inventoryService.save(dto);
        return ResponseEntity.created(URI.create("/api/inventories/" + response.getId())).body(response);
    }

    @Override
    @PatchMapping("/{id}/quantity")
    public ResponseEntity<InventoryResponseDTO> updateQuantity(@PathVariable UUID id,
                                                                 @Valid @RequestBody UpdateQuantityDTO dto) {
        return ResponseEntity.ok(inventoryService.updateQuantity(id, dto.getQuantity()));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        inventoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<InventoryResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(inventoryService.findById(id, companyId));
    }

    @Override
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<InventoryResponseDTO>> findBySupplier(@PathVariable UUID supplierId) {
        return ResponseEntity.ok(inventoryService.findBySupplier(supplierId));
    }

    @Override
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<InventoryResponseDTO>> findAllByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(inventoryService.findAllByCompany(companyId));
    }
}
