package com.solaria.persistence.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.solaria.persistence.exception.handler.ProblemDetailFactory;
import com.solaria.persistence.observability.HttpObservationErrors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Devolve o 401 de autenticação no formato {@link ProblemDetail} ({@code UNAUTHENTICATED})
 *
 * <p>
 * Chamado pelo Spring Security quando o token está ausente, inválido ou expirado -> roda antes do
 * Spring MVC, por isso a resposta é escrita direto no {@link HttpServletResponse}
 * </p>
 *
 * <p>Marca o span como erro e loga {@code WARN}, igual aos demais 4xx</p>
 */
@Component
public class ProblemDetailAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(ProblemDetailAuthenticationEntryPoint.class);

    private final ProblemDetailFactory problemDetailFactory;
    private final ObjectMapper objectMapper;

    public ProblemDetailAuthenticationEntryPoint(ProblemDetailFactory problemDetailFactory,
            ObjectMapper objectMapper) {
        this.problemDetailFactory = problemDetailFactory;
        this.objectMapper = objectMapper;
    }

    // Método chamado automaticamente pelo Spring Security quando o cliente
    // tenta acessar um endpoint protegido e não possui uma autenticação válida.
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        // 401 gerado na chain fora do GlobalExceptionHandler
        // marca a observação como span ERROR + tag exception, como WARN
        HttpObservationErrors.mark(request, authException);
        log.warn("401 em {} {}: {}", 
        request.getMethod(),
        request.getRequestURI(),
        authException.getMessage());

        // cria corpo do json a partir da classe ProblemDetail
        ProblemDetail problem = problemDetailFactory.create(
                HttpStatus.UNAUTHORIZED,
                "Autenticação necessária: token ausente, inválido ou expirado.",
                "UNAUTHENTICATED",
                null,
                request);
        // Escreve status/headers/corpo diretamente na resposta pois esse handler fica
        // na camada de filtro, antes de spring MVC
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(problem));
    }
}
