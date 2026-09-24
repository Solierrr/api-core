package com.solaria.persistence.observability;

import java.io.IOException;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.filter.OncePerRequestFilter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *<p>
 *  Filtro que copia o {@code traceId} do span ativo para o header de resposta {@code X-Trace-Id}
 * </p>
 * 
 * <p>
 * Pega o traceId do span já ativo e coloca no response
 * </p>
 * 
 * <p>
 * Sem span ativo, o filtro nao faz nada
 * </p>
 *
 * <p>
 * Registrado em {@link ObservabilityConfig#traceIdResponseFilterRegistration}
 * </p>
 */
public class TraceIdResponseFilter extends OncePerRequestFilter {

    /** Header de resposta com o {@code traceId} */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    // ObjectProvider -> bean Tracer nao existe quando o tracing esta desligado 
    private final ObjectProvider<Tracer> tracerProvider;

    public TraceIdResponseFilter(ObjectProvider<Tracer> tracerProvider) {
        this.tracerProvider = tracerProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // verifica se o tracer existe | se não existir a chain não faz nada
        Tracer tracer = tracerProvider.getIfAvailable();
        
        if (tracer != null && !response.isCommitted()) {
            // mantem o span atual 
            Span span = tracer.currentSpan();

            if (span != null) {

                response.setHeader(TRACE_ID_HEADER,
                 span.context().traceId()); // pega o traceId do span
            }
        }
        filterChain.doFilter(request, response);
    }
}
