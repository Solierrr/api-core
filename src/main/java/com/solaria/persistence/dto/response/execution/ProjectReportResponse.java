package com.solaria.persistence.dto.response.execution;

import java.time.OffsetDateTime;
import java.util.*;

/** Relatório do projeto; as imagens são fotos do projeto, lidas pelo endereço de conteúdo de cada uma. */
public record ProjectReportResponse(
        UUID id,
        String title,
        String content,
        String author,
        OffsetDateTime createdAt,
        List<ProjectPhotoResponse> photos) {}
