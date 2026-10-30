package com.solaria.persistence.controller.professional;

import java.util.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.solaria.persistence.service.professional.ProfessionalTeamService;
import com.solaria.persistence.service.professional.ProfessionalTeamService.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@Tag(name = "Equipes Profissionais", description = "Equipes de profissionais montadas pelas empresas")
public class ProfessionalTeamController {
    private final ProfessionalTeamService teams;
    @Operation(summary = "Cria uma equipe profissional")
    @PostMapping("/api/professional-teams")
    public Response create(@Valid @RequestBody Request request) { return teams.save(null, request); }
    @Operation(summary = "Edita uma equipe profissional")
    @PutMapping("/api/professional-teams/{id}")
    public Response update(@PathVariable UUID id, @Valid @RequestBody Request request) { return teams.save(id, request); }
    @Operation(summary = "Lista as equipes da empresa")
    @GetMapping("/api/professional-teams/company/{companyId}")
    public List<Response> company(@PathVariable UUID companyId) { return teams.company(companyId); }
    @Operation(summary = "Lista as equipes do catálogo")
    @GetMapping("/api/catalog/teams")
    public List<Response> catalog() { return teams.catalog(); }
}
