package com.solaria.persistence.integration.recommendation;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.RecommendationUnavailableException;
import com.solaria.persistence.exception.ResourceNotFoundException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

// Cliente dos feeds do api-recommendation; a chave nunca sai daqui nem entra em log
@Component
public class RecommendationClient {

    static final String KEY_HEADER = "X-Recommendation-Key";
    private static final Logger log = LoggerFactory.getLogger(RecommendationClient.class);
    private static final String FEED_UNAVAILABLE = "FEED_UNAVAILABLE";
    private static final String DEFAULT_UNAVAILABLE = "Não foi possível obter as recomendações agora. Tente novamente em instantes.";

    private final RestClient restClient;
    private final RecommendationClientProperties properties;
    private final ObjectMapper json;

    public RecommendationClient(RestClient recommendationRestClient, RecommendationClientProperties properties,
            ObjectMapper json) {
        this.restClient = recommendationRestClient;
        this.properties = properties;
        this.json = json;
    }

    // Feed autenticado; uma nova tentativa em falha de rede ou 502/503/504, e feed público se o grafo estiver desligado
    public JsonNode feed(String feed, Map<String, String> params) {
        if (!properties.enabled()) {
            throw new RecommendationUnavailableException("As recomendações não estão habilitadas neste ambiente.");
        }
        for (int attempt = 1;; attempt++) {
            try {
                return get("/feeds/" + feed, params, true);
            } catch (RestClientResponseException error) {
                int status = error.getStatusCode().value();
                if (status == 503 && FEED_UNAVAILABLE.equals(errorCode(error))) {
                    return publicFeed(feed, params);
                }
                if (attempt == 1 && (status == 502 || status == 503 || status == 504)) {
                    continue;
                }
                throw translate(feed, status, error);
            } catch (ResourceAccessException error) {
                if (attempt == 1) {
                    continue;
                }
                log.warn("api-recommendation inacessível ao buscar o feed {}", feed);
                throw new RecommendationUnavailableException(DEFAULT_UNAVAILABLE);
            }
        }
    }

    // Feed público equivalente, sem chave; só o filtro de profissão faz sentido nele
    private JsonNode publicFeed(String feed, Map<String, String> params) {
        Map<String, String> publicParams = params.containsKey("profession_id")
                ? Map.of("profession_id", params.get("profession_id"))
                : Map.of();
        try {
            return get("/public/feeds/" + feed, publicParams, false);
        } catch (RestClientException error) {
            log.warn("api-recommendation sem feed público para {}", feed);
            throw new RecommendationUnavailableException(DEFAULT_UNAVAILABLE);
        }
    }

    private JsonNode get(String path, Map<String, String> params, boolean authenticated) {
        RestClient.RequestHeadersSpec<?> request = restClient.get().uri(builder -> {
            builder.path(path);
            params.forEach(builder::queryParam);
            return builder.build();
        });
        if (authenticated && properties.apiKey() != null && !properties.apiKey().isBlank()) {
            request = request.header(KEY_HEADER, properties.apiKey());
        }
        JsonNode body = request.retrieve().body(JsonNode.class);
        if (body == null) {
            throw new RecommendationUnavailableException(DEFAULT_UNAVAILABLE);
        }
        return body;
    }

    // 404 e 422 do feed são erros de contexto do chamador; o resto (inclusive 401/403 de chave) vira indisponibilidade
    private RuntimeException translate(String feed, int status, RestClientResponseException error) {
        if (status == 404) {
            return new ResourceNotFoundException(
                    errorMessage(error, "Contexto de recomendação não encontrado."));
        }
        if (status == 422) {
            return new BusinessRuleException(
                    errorMessage(error, "Os parâmetros informados não permitem gerar a recomendação."));
        }
        log.error("api-recommendation respondeu {} ao buscar o feed {}", status, feed);
        return new RecommendationUnavailableException(DEFAULT_UNAVAILABLE);
    }

    private String errorCode(RestClientResponseException error) {
        return detail(error).path("code").asString("");
    }

    private String errorMessage(RestClientResponseException error, String fallback) {
        String message = detail(error).path("message").asString("");
        return message.isBlank() ? fallback : message;
    }

    private JsonNode detail(RestClientResponseException error) {
        try {
            return json.readTree(error.getResponseBodyAsString()).path("detail");
        } catch (RuntimeException unreadable) {
            return json.createObjectNode();
        }
    }
}
