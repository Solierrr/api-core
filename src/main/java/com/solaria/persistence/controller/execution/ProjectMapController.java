package com.solaria.persistence.controller.execution;

import com.solaria.persistence.dto.response.execution.ProjectResponse;
import com.solaria.persistence.service.execution.ProjectGeocodingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Tag(name = "Projetos")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectMapController {
    private final ProjectGeocodingService geocoding;

    @Operation(summary = "Resolve a localização do projeto no mapa a partir do endereço", operationId = "resolveProjectMapLocation")
    @PostMapping("/{id}/map-location")
    public ProjectResponse resolve(@PathVariable UUID id) { return geocoding.resolve(id); }
}
