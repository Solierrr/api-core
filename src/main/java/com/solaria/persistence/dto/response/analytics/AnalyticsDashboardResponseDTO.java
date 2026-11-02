package com.solaria.persistence.dto.response.analytics;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Indicadores Gold de uma persona. {@code scope} é PLATFORM, SUPPLIER ou REQUESTER; {@code kpis} mapeia o nome
 * do indicador para as linhas já filtradas pelo banco. {@code loadedAt} é a data da última carga (ISO-8601),
 * ou nulo se o banco não registrou.
 */
public record AnalyticsDashboardResponseDTO(
        String scope,
        UUID companyId,
        String loadedAt,
        Map<String, List<Map<String, Object>>> kpis) {
}
