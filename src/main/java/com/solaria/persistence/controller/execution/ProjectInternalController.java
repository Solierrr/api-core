package com.solaria.persistence.controller.execution;

import com.solaria.persistence.domain.enums.execution.*;
import com.solaria.persistence.repository.execution.*;
import com.solaria.persistence.repository.identity.*;
import com.solaria.persistence.service.execution.*;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/internal/projects")
@RequiredArgsConstructor
@SecurityRequirement(name = "serviceKey")
@Tag(name = "Acesso Interno a Projetos", description = "Endpoint M2M usado pelo api-messenger")
public class ProjectInternalController {
    private final ProjectService projects;
    private final ProjectAccess access;
    private final UserRepository users;
    private final ProjectMemberRepository members;
    private final UserCompanyRepository companies;

    @Operation(summary = "Consulta o papel do usuário no projeto e os participantes")
    @GetMapping("/{id}/access/{authId}")
    @Transactional(readOnly = true)
    public Map<String, Object> authority(@PathVariable UUID id, @PathVariable UUID authId) {
        var p = projects.entity(id, false);
        var u =
                users.findByAuthId(authId)
                        .orElseThrow(
                                () ->
                                        new com.solaria.persistence.exception
                                                .UnauthorizedAccessException(
                                                "Projeto indisponível"));
        var role = access.role(p, u);
        var roster = new LinkedHashMap<UUID, String>();
        for (var m : members.findByProjectIdAndActiveTrue(id)) {
            var member =
                    users.findById(m.getUserId())
                            .filter(v -> Boolean.TRUE.equals(v.getActive()))
                            .orElse(null);
            if (member != null) roster.put(member.getAuth_id(), m.getRole().name());
        }
        if (p.getOwnerCompanyId() != null)
            for (var l : companies.findByCompanyId(p.getOwnerCompanyId()))
                if (com.solaria.persistence.domain.entity.identity.Position.ADMIN_NAME.equals(
                                l.getPosition().getName())
                        && Boolean.TRUE.equals(l.getUser().getActive()))
                    roster.put(l.getUser().getAuth_id(), "OWNER");
        if (p.getOwnerUserId() != null)
            users.findById(p.getOwnerUserId())
                    .ifPresent(owner -> roster.put(owner.getAuth_id(), "OWNER"));
        boolean readOnly =
                p.isArchived()
                        || p.getStatus() == ServiceStatus.COMPLETED
                        || p.getStatus() == ServiceStatus.CANCELED;
        return Map.of(
                "role", role.name(), "readOnly", readOnly, "members", roster, "name", p.getName());
    }
}
