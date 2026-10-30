package com.solaria.persistence.exception.handler;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Monta o corpo JSON de todo erro da API no formato {@link ProblemDetail} (RFC 9457)
 *
 * <p>
 * {@code errorCode} identifica o erro, {@code timestamp} marca o instante da falha e {@code traceId}
 * leva direto ao trace no Grafana -> o cliente decide pelo {@code errorCode}, não pelo {@code detail}
 * </p>
 *
 * <p>
 * Sem {@code type} (omitido) e sem {@code title} próprio -> o Spring preenche {@code title} com a
 * reason phrase do status ("Not Found")
 * </p>
 */
@Component
public class ProblemDetailFactory {

    // cria corpo do json a partir da classe ProblemDetail
    public ProblemDetail create(HttpStatusCode status,
                                String detail,
                                String errorCode,
                                Map<String, Object> properties,
                                HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        if (request != null) {
            problem.setInstance(URI.create(request.getRequestURI()));
        }
        problem.setProperty("errorCode", errorCode);
        problem.setProperty("timestamp", Instant.now());
        // sem span ativo (tracing desligado) -> corpo sai sem traceId
        SpanContext span = Span.current().getSpanContext();
        if (span.isValid()) {
            problem.setProperty("traceId", span.getTraceId());
        }
        if (properties != null) {
            properties.forEach(problem::setProperty);
        }
        return problem;
    }
}
