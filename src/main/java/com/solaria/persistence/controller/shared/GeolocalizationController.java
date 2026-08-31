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

import com.solaria.persistence.dto.request.shared.GeolocalizationRequestDTO;
import com.solaria.persistence.dto.response.shared.GeolocalizationResponseDTO;
import com.solaria.persistence.openapi.shared.GeolocalizationOpenApi;
import com.solaria.persistence.service.shared.GeolocalizationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/geolocalizations")
@RequiredArgsConstructor
public class GeolocalizationController implements GeolocalizationOpenApi {

    private final GeolocalizationService geolocalizationService;

    @Override
    @PostMapping
    public ResponseEntity<GeolocalizationResponseDTO> save(@Valid @RequestBody GeolocalizationRequestDTO dto) {
        GeolocalizationResponseDTO response = geolocalizationService.save(dto);
        return ResponseEntity.created(URI.create("/api/geolocalizations/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<GeolocalizationResponseDTO> update(@PathVariable UUID id,
                                                               @Valid @RequestBody GeolocalizationRequestDTO dto) {
        return ResponseEntity.ok(geolocalizationService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        geolocalizationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<GeolocalizationResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(geolocalizationService.findById(id));
    }

    @Override
    @GetMapping("/address/{addressId}")
    public ResponseEntity<List<GeolocalizationResponseDTO>> findByAddress(@PathVariable UUID addressId) {
        return ResponseEntity.ok(geolocalizationService.findByAddress(addressId));
    }
}
