package com.solaria.persistence.controller.execution;

import com.solaria.persistence.dto.response.execution.ProjectResponse;
import com.solaria.persistence.service.execution.ProjectGeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectMapController {
    private final ProjectGeocodingService geocoding;

    @PostMapping("/{id}/map-location")
    public ProjectResponse resolve(@PathVariable UUID id) { return geocoding.resolve(id); }
}
