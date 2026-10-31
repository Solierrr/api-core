package com.solaria.persistence.service.execution;

import com.solaria.persistence.domain.entity.execution.ProjectReport;
import com.solaria.persistence.dto.response.execution.*;
import com.solaria.persistence.exception.*;
import com.solaria.persistence.repository.execution.*;
import com.solaria.persistence.repository.identity.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * Relatórios do projeto: título, conteúdo e imagens. As imagens viram fotos do projeto (aparecem em "Imagens do local")
 * e cada relatório entra no histórico, de onde pode ser aberto.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectReportService {
    static final int MAX_IMAGES = 5;

    private final ProjectService projects;
    private final ProjectAccess access;
    private final ProjectPhotoService photoService;
    private final ProjectReportRepository reports;
    private final ProjectPhotoRepository photos;
    private final UserRepository users;

    @Transactional(readOnly = true)
    public List<ProjectReportResponse> list(UUID projectId) {
        access.read(projects.entity(projectId, false));
        return reports.findByProjectIdOrderByCreatedAtDesc(projectId).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public ProjectReportResponse get(UUID projectId, UUID id) {
        access.read(projects.entity(projectId, false));
        return response(report(projectId, id));
    }

    public ProjectReportResponse create(UUID projectId, String title, String content, List<MultipartFile> files) {
        var p = projects.entity(projectId, true);
        access.contribute(p);
        var cleanTitle = title == null ? "" : title.strip();
        var cleanContent = content == null ? "" : content.strip();
        if (cleanTitle.isEmpty() || cleanTitle.length() > 160)
            throw new BusinessRuleException("Informe um título de até 160 caracteres");
        if (cleanContent.isEmpty() || cleanContent.length() > 4000)
            throw new BusinessRuleException("Informe o conteúdo, com até 4000 caracteres");
        var images = files == null ? List.<MultipartFile>of() : files.stream().filter(f -> !f.isEmpty()).toList();
        if (images.size() > MAX_IMAGES)
            throw new BusinessRuleException("Um relatório aceita até " + MAX_IMAGES + " imagens");
        var report = new ProjectReport();
        report.setProjectId(projectId);
        report.setAuthorId(access.user().getId());
        report.setTitle(cleanTitle);
        report.setContent(cleanContent);
        reports.save(report);
        images.forEach(file -> photoService.storeForReport(p, file, report.getId()));
        projects.record(p, "REPORT_ADDED", cleanTitle, report.getId());
        return response(report);
    }

    private ProjectReport report(UUID projectId, UUID id) {
        return reports.findById(id)
                .filter(r -> projectId.equals(r.getProjectId()))
                .orElseThrow(() -> new ResourceNotFoundException("Relatório indisponível"));
    }

    private ProjectReportResponse response(ProjectReport r) {
        return new ProjectReportResponse(
                r.getId(),
                r.getTitle(),
                r.getContent(),
                users.findById(r.getAuthorId()).map(u -> u.getUsername()).orElse("Usuário"),
                r.getCreatedAt(),
                photos.ofReport(r.getProjectId(), r.getId()));
    }
}
