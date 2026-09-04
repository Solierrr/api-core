package com.solaria.persistence.controller.shared;

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

import com.solaria.persistence.dto.request.shared.AddressRequestDTO;
import com.solaria.persistence.dto.response.shared.AddressResponseDTO;
import com.solaria.persistence.openapi.shared.AddressOpenApi;
import com.solaria.persistence.service.shared.AddressService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController implements AddressOpenApi {

    private final AddressService addressService;

    @Override
    @PostMapping
    public ResponseEntity<AddressResponseDTO> save(@Valid @RequestBody AddressRequestDTO dto) {
        AddressResponseDTO response = addressService.save(dto);
        return ResponseEntity.created(URI.create("/api/addresses/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody AddressRequestDTO dto) {
        return ResponseEntity.ok(addressService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        addressService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(addressService.findById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> findAll() {
        return ResponseEntity.ok(addressService.findAll());
    }
}
