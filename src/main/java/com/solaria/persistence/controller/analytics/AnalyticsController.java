package com.solaria.persistence.controller.analytics;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.response.analytics.AnalyticsDashboardResponseDTO;
import com.solaria.persistence.service.analytics.AnalyticsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Indicadores", description = "Dashboards de indicadores Gold da plataforma e das empresas")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Indicadores da plataforma (somente admin da plataforma)")
    @GetMapping("/platform")
    public ResponseEntity<AnalyticsDashboardResponseDTO> platform() {
        return ResponseEntity.ok(analyticsService.platform());
    }

    @Operation(summary = "Indicadores de uma empresa: o admin da empresa vê a própria; o admin da plataforma vê qualquer uma")
    @GetMapping("/companies/{companyId}")
    public ResponseEntity<AnalyticsDashboardResponseDTO> company(@PathVariable UUID companyId) {
        return ResponseEntity.ok(analyticsService.company(companyId));
    }
}
