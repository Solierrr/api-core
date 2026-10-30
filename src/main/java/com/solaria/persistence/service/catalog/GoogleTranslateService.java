package com.solaria.persistence.service.catalog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.solaria.persistence.config.GoogleTranslateProperties;
import com.solaria.persistence.exception.BusinessRuleException;

@Service
public class GoogleTranslateService {
    public record Field(@JsonProperty("field_name") String name, String text) {}
    public record Request(List<Field> fields) {}
    public record TranslatedField(@JsonProperty("field_name") String name, String language, String value) {}
    public record Response(@JsonProperty("source_language") String sourceLanguage, List<TranslatedField> translations) {}
    public record Result(String sourceLanguage, Map<String, Map<String, String>> translations) {}

    private static final Set<String> LANGUAGES = Set.of("pt", "en", "es");
    private final RestClient googleTranslateRestClient;
    private final GoogleTranslateProperties properties;

    public GoogleTranslateService(RestClient googleTranslateRestClient, GoogleTranslateProperties properties) {
        this.googleTranslateRestClient = googleTranslateRestClient;
        this.properties = properties;
    }

    public Result translateFields(Map<String, String> fields) {
        if (properties.getConsumerToken() == null || properties.getConsumerToken().isBlank()) {
            throw new BusinessRuleException("Google registry consumer token is not configured");
        }
        try {
            Response response = googleTranslateRestClient.post().uri("/v1/i18n/translate")
                    .headers(headers -> {
                        headers.setBearerAuth(properties.getConsumerToken());
                        if (properties.getServiceKey() != null && !properties.getServiceKey().isBlank()) {
                            headers.set("X-Service-Key", properties.getServiceKey());
                        }
                    })
                    .body(new Request(fields.entrySet().stream().map(entry -> new Field(entry.getKey(), entry.getValue())).toList()))
                    .retrieve().body(Response.class);
            if (response == null || response.sourceLanguage() == null || response.sourceLanguage().isBlank()
                    || response.translations() == null) throw new BusinessRuleException("Invalid registry translation response");
            Map<String, Map<String, String>> translated = new LinkedHashMap<>();
            if (LANGUAGES.contains(response.sourceLanguage())) {
                translated.put(response.sourceLanguage(), new LinkedHashMap<>(fields));
            }
            for (TranslatedField field : response.translations()) {
                if (!LANGUAGES.contains(field.language()) || !fields.containsKey(field.name()) || field.value() == null
                        || field.value().isBlank()) throw new BusinessRuleException("Invalid registry translation field");
                Map<String, String> values = translated.computeIfAbsent(field.language(), ignored -> new LinkedHashMap<>());
                if (values.putIfAbsent(field.name(), field.value()) != null) {
                    throw new BusinessRuleException("Duplicate registry translation field");
                }
            }
            if (!translated.keySet().equals(LANGUAGES)
                    || translated.values().stream().anyMatch(values -> !values.keySet().equals(fields.keySet()))) {
                throw new BusinessRuleException("Incomplete registry translations");
            }
            return new Result(response.sourceLanguage(), translated);
        } catch (BusinessRuleException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new BusinessRuleException("Could not translate offer through google-registry");
        }
    }
}
