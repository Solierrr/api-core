package com.solaria.persistence.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Gera slugs a partir de nomes (empresa, pessoa), no mesmo padrão usado no front-end
 * (normalização NFD, remoção de acentos, minúsculas, apenas [a-z0-9-]).
 */
public final class SlugUtil {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_HYPHENS = Pattern.compile("^-+|-+$");

    private SlugUtil() {}

    public static String slugify(String input) {
        if (input == null) {
            return null;
        }
        String normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD);
        normalized = DIACRITICS.matcher(normalized).replaceAll("");
        normalized = normalized.toLowerCase();
        normalized = NON_ALPHANUMERIC.matcher(normalized).replaceAll("-");
        normalized = EDGE_HYPHENS.matcher(normalized).replaceAll("");
        return normalized;
    }
}
