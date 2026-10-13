package com.solaria.persistence.controller.execution;

import com.solaria.persistence.domain.enums.execution.*;
import com.solaria.persistence.repository.execution.*;
import com.solaria.persistence.repository.identity.*;
import com.solaria.persistence.service.execution.*;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@RestController
@RequestMapping("/internal/projects")
@RequiredArgsConstructor
public class ProjectInternalController {
    private final ProjectService projects;
    private final ProjectAccess access;
    private final UserRepository users;
    private final ProjectMemberRepository members;
    private final UserCompanyRepository companies;

    @Value("${app.projects.internal-key:}")
    private String key;

    @GetMapping("/{id}/access/{authId}")
    @Transactional(readOnly = true)
    public Map<String, Object> authority(
            @PathVariable UUID id,
            @PathVariable UUID authId,
            @RequestHeader(value = "X-Project-Service-Key", defaultValue = "") String provided) {
        if (key.length() < 32
                || !MessageDigest.isEqual(
                        key.getBytes(StandardCharsets.UTF_8),
                        provided.getBytes(StandardCharsets.UTF_8)))
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Service authentication required");
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
