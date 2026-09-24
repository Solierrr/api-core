package com.solaria.persistence.service.catalog;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.solaria.persistence.config.GoogleTranslateProperties;
import com.solaria.persistence.exception.BusinessRuleException;

/**
 * Cliente fino da Google Cloud Translation API (v2): detecção de idioma e tradução de texto.
 * Usado pelo OfferTranslationService para preencher as traduções de uma oferta em background.
 */
@Service
public class GoogleTranslateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleTranslateService.class);

    private final RestClient googleTranslateRestClient;
    private final GoogleTranslateProperties properties;

    public GoogleTranslateService(RestClient googleTranslateRestClient, GoogleTranslateProperties properties) {
        this.googleTranslateRestClient = googleTranslateRestClient;
        this.properties = properties;
    }

    /**
     * Detecta o idioma de um texto. Retorna o código curto ISO-639-1 (ex.: "pt", "en", "es").
     */
    @SuppressWarnings("unchecked")
    public String detectLanguage(String text) {
        try {
            Map<String, Object> response = googleTranslateRestClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/detect").queryParam("key", properties.getApiKey()).build())
                    .body(Map.of("q", text))
                    .retrieve()
                    .body(Map.class);

            Map<String, Object> data = response == null ? null : (Map<String, Object>) response.get("data");
            List<List<Map<String, Object>>> detections = data == null
                    ? null : (List<List<Map<String, Object>>>) data.get("detections");

            if (detections == null || detections.isEmpty() || detections.get(0).isEmpty()) {
                throw new BusinessRuleException("Google Translate não retornou uma detecção de idioma válida");
            }

            String language = (String) detections.get(0).get(0).get("language");
            if (language == null || language.isBlank()) {
                throw new BusinessRuleException("Google Translate não retornou uma detecção de idioma válida");
            }
            return language;
        } catch (BusinessRuleException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            LOGGER.error("Falha ao detectar idioma via Google Translate", ex);
            throw new BusinessRuleException("Não foi possível detectar o idioma do texto informado");
        }
    }

    /**
     * Traduz uma lista de textos (mantendo a ordem) de sourceShortCode para targetShortCode.
     * Os códigos são curtos (ISO-639-1), ex.: "pt", "en", "es".
     */
    @SuppressWarnings("unchecked")
    public List<String> translate(List<String> texts, String sourceShortCode, String targetShortCode) {
        try {
            Map<String, Object> body = Map.of(
                    "q", texts,
                    "source", sourceShortCode,
                    "target", targetShortCode,
                    "format", "text");

            Map<String, Object> response = googleTranslateRestClient.post()
                    .uri(uriBuilder -> uriBuilder.queryParam("key", properties.getApiKey()).build())
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            Map<String, Object> data = response == null ? null : (Map<String, Object>) response.get("data");
            List<Map<String, Object>> translations = data == null
                    ? null : (List<Map<String, Object>>) data.get("translations");

            if (translations == null || translations.size() != texts.size()) {
                throw new BusinessRuleException(
                        "Google Translate não retornou o número esperado de traduções");
            }

            return translations.stream().map(t -> (String) t.get("translatedText")).toList();
        } catch (BusinessRuleException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            LOGGER.error("Falha ao traduzir texto via Google Translate ({} -> {})", sourceShortCode, targetShortCode, ex);
            throw new BusinessRuleException("Não foi possível traduzir o conteúdo informado");
        }
    }
}
