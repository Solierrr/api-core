package com.solaria.persistence.controller.execution;

import com.solaria.persistence.dto.request.execution.*;
import com.solaria.persistence.dto.response.execution.*;
import com.solaria.persistence.openapi.execution.ProjectOpenApi;
import com.solaria.persistence.service.execution.*;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController implements ProjectOpenApi {
    private final ProjectService projects;
    private final ProjectInvitationService invites;
    private final ProjectInviteLinkService inviteLinks;
    private final ProjectActivityService agenda;
    private final ProjectPhotoService photos;

    @GetMapping("/options")
    public Map<String, Object> options(@RequestParam(required = false) UUID companyId) {
        return projects.options(companyId);
    }

    @GetMapping
    public List<ProjectResponse> list(@RequestParam(required = false) UUID companyId) {
        return projects.list(companyId);
    }

    @PostMapping
    public ProjectResponse create(@Valid @RequestBody ProjectRequest d) {
        return projects.create(d);
    }

    @GetMapping("/{id}")
    public ProjectResponse detail(@PathVariable UUID id) {
        return projects.detail(id);
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable UUID id, @Valid @RequestBody ProjectRequest d) {
        return projects.update(id, d);
    }

    @PatchMapping("/{id}/status")
    public ProjectResponse status(
            @PathVariable UUID id, @Valid @RequestBody ProjectDecisionRequest d) {
        return projects.status(id, d.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(@PathVariable UUID id) {
        projects.archive(id);
    }

    @GetMapping("/{id}/members")
    public List<ProjectMemberResponse> members(@PathVariable UUID id) {
        return projects.memberList(id);
    }

    @PatchMapping("/{id}/members/{userId}")
    public ProjectMemberResponse member(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @Valid @RequestBody ProjectMemberRequest d) {
        return projects.updateMember(id, userId, d);
    }

    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable UUID id, @PathVariable UUID userId) {
        projects.removeMember(id, userId);
    }

    @GetMapping("/invitations/me")
    public List<ProjectInvitationResponse> myInvites() {
        return invites.mine();
    }

    @GetMapping("/{id}/invitations")
    public List<ProjectInvitationResponse> invitations(@PathVariable UUID id) {
        return invites.list(id);
    }

    @PostMapping("/{id}/invitations")
    public List<ProjectInvitationResponse> invite(
            @PathVariable UUID id, @Valid @RequestBody ProjectInvitationRequest d) {
        return invites.create(id, d);
    }

    @PostMapping("/invitations/{id}/response")
    public ProjectInvitationResponse respond(@PathVariable UUID id, @RequestParam boolean accept) {
        return invites.respond(id, accept);
    }

    @DeleteMapping("/invitations/{id}")
    public ProjectInvitationResponse revoke(@PathVariable UUID id) {
        return invites.revoke(id);
    }

    @PostMapping("/invitations/{id}/resend")
    public ProjectInvitationResponse resend(@PathVariable UUID id) {
        return invites.resend(id);
    }

    @GetMapping("/{id}/invite-links")
    public List<ProjectInviteLinkResponse> inviteLinks(@PathVariable UUID id) {
        return inviteLinks.list(id);
    }

    @PostMapping("/{id}/invite-links")
    public ProjectInviteLinkResponse createInviteLink(
            @PathVariable UUID id, @Valid @RequestBody ProjectInviteLinkRequest d) {
        return inviteLinks.create(id, d);
    }

    @DeleteMapping("/{id}/invite-links/{linkId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeInviteLink(@PathVariable UUID id, @PathVariable UUID linkId) {
        inviteLinks.revoke(id, linkId);
    }

    @PostMapping("/invite-links/preview")
    public ProjectInviteLinkPreviewResponse previewInviteLink(
            @Valid @RequestBody ProjectInviteLinkTokenRequest d) {
        return inviteLinks.preview(d.token());
    }

    @PostMapping("/invite-links/redeem")
    public ProjectResponse redeemInviteLink(@Valid @RequestBody ProjectInviteLinkTokenRequest d) {
        return inviteLinks.redeem(d.token());
    }

    @GetMapping("/{id}/activities")
    public List<ProjectActivityResponse> activities(@PathVariable UUID id) {
        return agenda.list(id);
    }

    @PostMapping("/{id}/activities")
    public ProjectActivityResponse activity(
            @PathVariable UUID id, @Valid @RequestBody ProjectActivityRequest d) {
        return agenda.save(id, null, d);
    }

    @PutMapping("/{id}/activities/{activityId}")
    public ProjectActivityResponse updateActivity(
            @PathVariable UUID id,
            @PathVariable UUID activityId,
            @Valid @RequestBody ProjectActivityRequest d) {
        return agenda.save(id, activityId, d);
    }

    @PatchMapping("/{id}/activities/{activityId}/status")
    public ProjectActivityResponse activityStatus(
            @PathVariable UUID id,
            @PathVariable UUID activityId,
            @Valid @RequestBody ProjectDecisionRequest d) {
        return agenda.status(id, activityId, d.status());
    }

    @GetMapping("/{id}/history")
    public List<Map<String, Object>> history(@PathVariable UUID id) {
        return projects.timeline(id);
    }

    @GetMapping("/{id}/photos")
    public List<ProjectPhotoResponse> photoList(@PathVariable UUID id) {
        return photos.list(id);
    }

    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProjectPhotoResponse upload(
            @PathVariable UUID id,
            @RequestPart MultipartFile file,
            @RequestParam(defaultValue = "false") boolean cover) {
        return photos.upload(id, file, cover);
    }

    @GetMapping("/{id}/photos/{photoId}/content")
    public ResponseEntity<byte[]> photo(@PathVariable UUID id, @PathVariable UUID photoId) {
        var p = photos.read(id, photoId);
        return ResponseEntity.ok()
                .header("Cache-Control", "private, no-store")
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Disposition", "inline")
                .contentType(MediaType.parseMediaType(p.getMime()))
                .body(p.getContent());
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePhoto(@PathVariable UUID id, @PathVariable UUID photoId) {
        photos.remove(id, photoId);
    }

    @PatchMapping("/{id}/photos/{photoId}/cover")
    public ProjectPhotoResponse cover(@PathVariable UUID id, @PathVariable UUID photoId) {
        return photos.cover(id, photoId);
    }
}
