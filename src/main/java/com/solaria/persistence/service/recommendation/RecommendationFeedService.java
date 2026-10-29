package com.solaria.persistence.service.recommendation;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.solaria.persistence.domain.entity.unit.LocalUnit;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.RecommendationUnavailableException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.integration.recommendation.RecommendationClient;
import com.solaria.persistence.integration.recommendation.RecommendationClientProperties;
import com.solaria.persistence.repository.unit.LocalUnitRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

import tools.jackson.databind.JsonNode;

// Feeds de recomendação do usuário autenticado: deriva empresa e unidade local, injeta o contexto no api-recommendation, cai no ranking simples do Postgres quando não há grafo e guarda a resposta por pouco tempo
@Service
public class RecommendationFeedService {

    private static final Pattern STRATEGY = Pattern.compile("[a-z][a-z_]{0,31}");
    private static final int MAX_CACHED_FEEDS = 256;

    private final RecommendationClient client;
    private final RecommendationFallbackService fallback;
    private final RbacAuthorizationService rbac;
    private final LocalUnitRepository localUnits;
    private final RecommendationClientProperties properties;
    private final Clock clock;
    private final Map<String, CachedFeed> cache = new ConcurrentHashMap<>();

    @Autowired
    public RecommendationFeedService(RecommendationClient client, RecommendationFallbackService fallback,
            RbacAuthorizationService rbac, LocalUnitRepository localUnits, RecommendationClientProperties properties) {
        this(client, fallback, rbac, localUnits, properties, Clock.systemUTC());
    }

    RecommendationFeedService(RecommendationClient client, RecommendationFallbackService fallback,
            RbacAuthorizationService rbac, LocalUnitRepository localUnits, RecommendationClientProperties properties,
            Clock clock) {
        this.client = client;
        this.fallback = fallback;
        this.rbac = rbac;
        this.localUnits = localUnits;
        this.properties = properties;
        this.clock = clock;
    }

    public JsonNode professionals(String strategy, UUID professionId) {
        UUID companyId = rbac.findCurrentCompanyId().orElse(null);
        return feed("professionals", params(strategy, companyId, null, professionId),
                () -> fallback.professionals(strategy, companyId, professionId));
    }

    public JsonNode offers(String strategy, UUID localUnitId) {
        return unitFeed("offers", strategy, localUnitId);
    }

    public JsonNode suppliers(String strategy, UUID localUnitId) {
        return unitFeed("suppliers", strategy, localUnitId);
    }

    private JsonNode unitFeed(String feed, String strategy, UUID requestedUnitId) {
        UUID companyId = rbac.findCurrentCompanyId().orElse(null);
        UUID localUnitId = resolveLocalUnit(requestedUnitId, companyId);
        return feed(feed, params(strategy, companyId, localUnitId, null),
                () -> "offers".equals(feed) ? fallback.offers(strategy, companyId)
                        : fallback.suppliers(strategy, companyId));
    }

    private JsonNode feed(String feed, Map<String, String> params, Supplier<JsonNode> withoutGraph) {
        String key = feed + params;
        Instant now = clock.instant();
        CachedFeed cached = cache.get(key);
        if (cached != null && cached.expiresAt().isAfter(now)) {
            return cached.body();
        }
        JsonNode body;
        try {
            body = client.feed(feed, params);
        } catch (RecommendationUnavailableException unavailable) {
            body = withoutGraph.get();
        }
        if (!properties.cacheTtl().isZero() && !properties.cacheTtl().isNegative()) {
            if (cache.size() >= MAX_CACHED_FEEDS) {
                cache.values().removeIf(entry -> !entry.expiresAt().isAfter(now));
                if (cache.size() >= MAX_CACHED_FEEDS) {
                    cache.clear();
                }
            }
            cache.put(key, new CachedFeed(body, now.plus(properties.cacheTtl())));
        }
        return body;
    }

    // Parâmetros na ordem fixa, para a chave de cache ser estável
    private Map<String, String> params(String strategy, UUID companyId, UUID localUnitId, UUID professionId) {
        Map<String, String> params = new LinkedHashMap<>();
        if (strategy != null && !strategy.isBlank()) {
            if (!STRATEGY.matcher(strategy).matches()) {
                throw new InvalidFieldException("strategy inválida.");
            }
            params.put("strategy", strategy);
        }
        if (professionId != null) {
            params.put("profession_id", professionId.toString());
        }
        if (localUnitId != null) {
            params.put("local_unit_id", localUnitId.toString());
        }
        if (companyId != null) {
            params.put("company_id", companyId.toString());
        }
        return params;
    }

    // Unidade informada precisa ser da empresa do usuário; sem informar, vale a única unidade da empresa, se houver só uma
    private UUID resolveLocalUnit(UUID requested, UUID companyId) {
        if (requested != null) {
            if (companyId == null || localUnits.findByIdAndRequester_Company_Id(requested, companyId).isEmpty()) {
                throw new ResourceNotFoundException("Unidade Local não encontrada com ID: " + requested);
            }
            return requested;
        }
        if (companyId == null) {
            return null;
        }
        List<LocalUnit> units = localUnits.findByRequester_Company_Id(companyId);
        return units.size() == 1 ? units.getFirst().getId() : null;
    }

    private record CachedFeed(JsonNode body, Instant expiresAt) {
    }
}
