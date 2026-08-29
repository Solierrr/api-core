package com.solaria.persistence.util;

import java.util.List;
import java.util.Map;

/**
 * Idiomas suportados pela plataforma, espelhando SUPPORTED_LANGUAGES do front-end
 * (web-app/src/config/locales/languages.ts).
 */
public final class SupportedLocales {

    public static final String PT_BR = "pt-BR";
    public static final String EN_US = "en-US";
    public static final String ES_ES = "es-ES";

    public static final List<String> ALL = List.of(PT_BR, EN_US, ES_ES);

    public static final String DEFAULT_LOCALE = PT_BR;

    /** Mapa de código curto (ISO-639-1, devolvido pela Google Translate API) para o locale suportado. */
    private static final Map<String, String> SHORT_CODE_TO_LOCALE = Map.of(
            "pt", PT_BR,
            "en", EN_US,
            "es", ES_ES
    );

    private SupportedLocales() {}

    /**
     * Resolve um código curto devolvido pela Google Translate API (ex.: "pt") para um dos
     * locales suportados. Quando o idioma detectado não é suportado, cai no DEFAULT_LOCALE.
     */
    public static String resolveFromShortCode(String shortCode) {
        if (shortCode == null) {
            return DEFAULT_LOCALE;
        }
        return SHORT_CODE_TO_LOCALE.getOrDefault(shortCode.toLowerCase(), DEFAULT_LOCALE);
    }

    public static List<String> targetsExcluding(String locale) {
        return ALL.stream().filter(l -> !l.equals(locale)).toList();
    }
}
