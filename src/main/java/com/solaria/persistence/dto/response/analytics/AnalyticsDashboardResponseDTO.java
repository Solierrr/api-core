package com.solaria.persistence.dto.response.analytics;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Indicadores Gold de uma persona. {@code scope} é PLATFORM, SUPPLIER ou REQUESTER; {@code kpis} mapeia o nome
 * do indicador para as linhas já filtradas pelo banco. {@code loadedAt} é a data da última carga (ISO-8601),
 * ou nulo se o banco não registrou. {@code names} traduz o id de fornecedor ou demandante no nome fantasia da
 * empresa, e só vem preenchido no dashboard da plataforma.
 */
public record AnalyticsDashboardResponseDTO(
        String scope,
        UUID companyId,
        String loadedAt,
        Map<String, List<Map<String, Object>>> kpis,
        Map<String, String> names) {
}
