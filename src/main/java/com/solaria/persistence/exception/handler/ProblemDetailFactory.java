package com.solaria.persistence.exception.handler;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Classe responsável por montar corpos de Jsons de erro.
 * Campo com Endpoints de erros será provavelmente inutilizado nas próximas versões da API [refinar]
 */
@Component
public class ProblemDetailFactory {

    private static final String TYPE_BASE = "https://solier.com/errors/";

    // cria corpo do json a partir da classe ProblemDetail
    public ProblemDetail create(HttpStatusCode status,
                                String detail,
                                String errorCode,
                                Map<String, Object> properties,
                                HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(errorCode);
        problem.setType(URI.create(TYPE_BASE + toKebabCase(errorCode)));
        if (request != null) {
            problem.setInstance(URI.create(request.getRequestURI()));
        }
        problem.setProperty("errorCode", errorCode);
        problem.setProperty("timestamp", Instant.now());
        if (properties != null) {
            properties.forEach(problem::setProperty);
        }
        return problem;
    }

    // Método de apoio para transformar ERROR_CODE em URL

    private static String toKebabCase(String errorCode) {
        if (errorCode == null || errorCode.isBlank()) {
            return "unknown";
        }
        return errorCode.toLowerCase().replace('_', '-');
    }
}
