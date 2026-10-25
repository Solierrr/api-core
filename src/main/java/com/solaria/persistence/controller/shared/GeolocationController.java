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

import com.solaria.persistence.dto.request.shared.GeolocationRequestDTO;
import com.solaria.persistence.dto.response.shared.GeolocationResponseDTO;
import com.solaria.persistence.openapi.shared.GeolocationOpenApi;
import com.solaria.persistence.service.shared.GeolocationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/geolocations")
@RequiredArgsConstructor
public class GeolocationController implements GeolocationOpenApi {

    private final GeolocationService geolocationService;

    @Override
    @PostMapping
    public ResponseEntity<GeolocationResponseDTO> save(@Valid @RequestBody GeolocationRequestDTO dto) {
        GeolocationResponseDTO response = geolocationService.save(dto);
        return ResponseEntity.created(URI.create("/api/geolocations/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<GeolocationResponseDTO> update(@PathVariable UUID id,
                                                               @Valid @RequestBody GeolocationRequestDTO dto) {
        return ResponseEntity.ok(geolocationService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        geolocationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<GeolocationResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(geolocationService.findById(id));
    }

    @Override
    @GetMapping("/address/{addressId}")
    public ResponseEntity<List<GeolocationResponseDTO>> findByAddress(@PathVariable UUID addressId) {
        return ResponseEntity.ok(geolocationService.findByAddress(addressId));
    }
}
