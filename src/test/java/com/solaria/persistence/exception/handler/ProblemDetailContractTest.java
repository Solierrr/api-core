package com.solaria.persistence.exception.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.security.ProblemDetailAccessDeniedHandler;
import com.solaria.persistence.security.ProblemDetailAuthenticationEntryPoint;

import tools.jackson.databind.ObjectMapper;

class ProblemDetailContractTest {

    private final ProblemDetailFactory factory = new ProblemDetailFactory();
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(factory);
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/models");

    @Test
    void logsTheSameStableCodeAsTheResponse() {
        var logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);
        var events = new ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent>();
        events.start();
        logger.addAppender(events);
        try {
            var response = handler.handleBusinessException(new BusinessRuleException("Modelo indisponível", "MODEL_NOT_EDITABLE"), request);
            var entry = events.list.getFirst();
            assertEquals(ch.qos.logback.classic.Level.WARN, entry.getLevel());
            var code = entry.getKeyValuePairs().stream().filter(pair -> "errorCode".equals(pair.key)).findFirst().orElseThrow();
            assertEquals(response.getBody().getProperties().get("errorCode"), code.value);
        } finally {
            logger.detachAppender(events);
            events.stop();
        }
    }

    @Test
    void businessErrorsPreserveTheirContextWithoutAHostedErrorUrl() {
        var response = handler.handleBusinessException(
                new UnauthorizedAccessException("Sem acesso à empresa.", Map.of("companyId", "company-1")),
                request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ProblemDetail body = response.getBody();
        assertNotNull(body);
        assertEquals(403, body.getStatus());
        assertNull(body.getType());
        assertEquals(URI.create("/api/models"), body.getInstance());
        assertEquals("ACCESS_DENIED", body.getTitle());
        assertEquals("ACCESS_DENIED", body.getProperties().get("errorCode"));
        assertEquals("Sem acesso à empresa.", body.getDetail());
        assertEquals("company-1", body.getProperties().get("companyId"));
        assertNotNull(body.getProperties().get("timestamp"));
    }

    @Test
    void mvcAndSecurityUseTheSameForbiddenErrorCode() throws Exception {
        ProblemDetail mvcBody = handler.handleAccessDenied(new AccessDeniedException("Denied"), request).getBody();
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any(ProblemDetail.class))).thenReturn("{}");
        var response = new MockHttpServletResponse();

        new ProblemDetailAccessDeniedHandler(factory, mapper)
                .handle(request, response, new AccessDeniedException("Denied"));

        var captured = ArgumentCaptor.forClass(ProblemDetail.class);
        verify(mapper).writeValueAsString(captured.capture());
        assertEquals(403, response.getStatus());
        assertEquals("application/problem+json", response.getContentType().split(";")[0]);
        assertNotNull(mvcBody);
        assertEquals("ACCESS_DENIED", mvcBody.getProperties().get("errorCode"));
        assertEquals(mvcBody.getProperties().get("errorCode"), captured.getValue().getProperties().get("errorCode"));
        assertNull(captured.getValue().getType());
        assertEquals("ACCESS_DENIED", new UnauthorizedAccessException("Denied").getErrorCode());
    }

    @Test
    void authenticationRemainsDistinctFromForbiddenAccess() throws Exception {
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any(ProblemDetail.class))).thenReturn("{}");
        var response = new MockHttpServletResponse();

        new ProblemDetailAuthenticationEntryPoint(factory, mapper)
                .commence(request, response, new BadCredentialsException("Invalid token"));

        var captured = ArgumentCaptor.forClass(ProblemDetail.class);
        verify(mapper).writeValueAsString(captured.capture());
        assertEquals(401, response.getStatus());
        assertEquals("UNAUTHENTICATED", captured.getValue().getProperties().get("errorCode"));
        assertNull(captured.getValue().getType());
    }
}
