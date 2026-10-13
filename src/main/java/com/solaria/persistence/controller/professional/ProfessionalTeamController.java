package com.solaria.persistence.controller.professional;

import java.util.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.solaria.persistence.service.professional.ProfessionalTeamService;
import com.solaria.persistence.service.professional.ProfessionalTeamService.*;

@RestController
@RequiredArgsConstructor
public class ProfessionalTeamController {
    private final ProfessionalTeamService teams;
    @PostMapping("/api/professional-teams")
    public Response create(@Valid @RequestBody Request request) { return teams.save(null, request); }
    @PutMapping("/api/professional-teams/{id}")
    public Response update(@PathVariable UUID id, @Valid @RequestBody Request request) { return teams.save(id, request); }
    @GetMapping("/api/professional-teams/company/{companyId}")
    public List<Response> company(@PathVariable UUID companyId) { return teams.company(companyId); }
    @GetMapping("/api/catalog/teams")
    public List<Response> catalog() { return teams.catalog(); }
}
