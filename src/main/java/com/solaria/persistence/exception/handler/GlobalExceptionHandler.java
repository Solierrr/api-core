package com.solaria.persistence.exception.handler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.solaria.persistence.exception.BusinessException;
import com.solaria.persistence.observability.HttpObservationErrors;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Classe responsável por centralizar tratamento de exceções
 * Diferentes handlers foram implementados visando cobrir escopos de: Banco de
 * Dados,
 * Regras de negócio, Jsons, BEAN validations e erros inesperados.
 *
 * Logging -> toda exceção tratada é registrada como erro no tracing e nas
 * métricas (span {@code ERROR} + tag {@code exception} em
 * {@code http.server.requests})
 * gerando um log correlacionado ao trace
 * 
 * {@code WARN} para 4xx
 * {@code ERROR} com stack trace para 5xx
 */

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String VALIDATION_FAILED = "VALIDATION_FAILED";

    private final ProblemDetailFactory factory;

    public GlobalExceptionHandler(ProblemDetailFactory factory) {
        this.factory = factory;
    }

    // Informa à observabilidade que a request teve um erro
    private void markObservationError(Throwable ex, HttpServletRequest request) {
        HttpObservationErrors.mark(request, ex);
    }

    // Marca a observação como erro e registra um log correlacionado ao trace
    private void recordError(Throwable ex, HttpStatusCode status, HttpServletRequest request) {
        markObservationError(ex, request);
        String method = (request != null) ? request.getMethod() : "?";
        String uri = (request != null) ? request.getRequestURI() : "?";
        if (status.is5xxServerError()) {
            log.error("Falha {} em {} {}", status.value(), method, uri, ex);
        } else {
            log.warn("Erro {} em {} {}: {}", status.value(), method, uri, ex.getMessage());
        }
    }

    // Handler responsável por BusinessException e todas as subclasses.

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(BusinessException ex,
            HttpServletRequest request) {

        recordError(ex, ex.getStatus(), request);

        ProblemDetail body = factory.create(
                ex.getStatus(),
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getProperties(),
                request);
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    // Handler responsável por exceções vindas do banco de dados.

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex,
            HttpServletRequest request) {

        recordError(ex, HttpStatus.CONFLICT, request);

        ProblemDetail body = factory.create(HttpStatus.CONFLICT,
                "A operação viola uma restrição do banco.",
                "DATA_INTEGRITY_VIOLATION",
                null,
                request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleOptimisticLock(OptimisticLockingFailureException ex,
            HttpServletRequest request) {

        recordError(ex, HttpStatus.CONFLICT, request);

        ProblemDetail body = factory.create(HttpStatus.CONFLICT,
                "O registro foi modificado por outra operação simultânea. Recarregue e tente novamente.",
                "CONCURRENT_MODIFICATION",
                null,
                request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEntityNotFound(EntityNotFoundException ex,
            HttpServletRequest request) {
        recordError(ex, HttpStatus.NOT_FOUND, request);

        ProblemDetail body = factory.create(HttpStatus.NOT_FOUND,
                "Registro não encontrado.",
                "NOT_FOUND",
                null,
                request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // Handler para negação de autorização vinda do Spring Security

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex,
            HttpServletRequest request) {
        recordError(ex, HttpStatus.FORBIDDEN, request);

        ProblemDetail body = factory.create(HttpStatus.FORBIDDEN,
                "Você não tem permissão para executar esta ação.",
                "FORBIDDEN",
                null,
                request);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // Handler para erros inesperados

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) {

        // registra no span (status ERROR + evento de excecao) com stack trace
        markObservationError(ex, request);
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);

        ProblemDetail body = factory.create(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado. Se persistir, contate o suporte.",
                "INTERNAL_ERROR",
                null,
                request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // Sobrescrita de handler responsável por BEAN validation / corpo da requisição.

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
                
        recordError(ex, HttpStatus.UNPROCESSABLE_CONTENT, servletRequest(request));

        List<Map<String, Object>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        ProblemDetail body = factory.create(HttpStatus.UNPROCESSABLE_CONTENT,
                "Erro de validação nos campos da requisição.",
                VALIDATION_FAILED,
                Map.of("errors", errors),
                servletRequest(request));
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    // Sobrescrita de handler responsável por BEAN validation / parâmetros dos
    // métodos.

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        recordError(ex, HttpStatus.UNPROCESSABLE_CONTENT, servletRequest(request));

        List<Map<String, Object>> errors = ex.getParameterValidationResults()
                .stream()
                .flatMap(result -> result instanceof ParameterErrors beanErrors
                        // corpo com @Valid validado junto dos parâmetros: reporta os campos do corpo
                        ? beanErrors.getAllErrors().stream()
                                .map(err -> fieldError(err instanceof FieldError fe ? fe.getField() : "payload",
                                        err.getDefaultMessage()))
                        : result.getResolvableErrors().stream()
                                .map(err -> fieldError(result.getMethodParameter().getParameterName(),
                                        err.getDefaultMessage())))
                .toList();
        ProblemDetail body = factory.create(HttpStatus.UNPROCESSABLE_CONTENT,
                "Erro de validação nos parâmetros da requisição.",
                VALIDATION_FAILED,
                Map.of("errors", errors), servletRequest(request));
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    // Sobrescrita de Handler responsável por JSONS inválidos.

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        recordError(ex, HttpStatus.UNPROCESSABLE_CONTENT, servletRequest(request));

        ProblemDetail body = factory.create(HttpStatus.UNPROCESSABLE_CONTENT,
                "Corpo da requisição malformado ou ilegível.",
                "MALFORMED_REQUEST",
                null,
                servletRequest(request));
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    // Parâmetro de rota/query com tipo inválido (ex.: UUID malformado); não ecoa o valor recebido

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        recordError(ex, HttpStatus.UNPROCESSABLE_CONTENT, servletRequest(request));

        ProblemDetail body = factory.create(HttpStatus.UNPROCESSABLE_CONTENT,
                "Parâmetro da requisição com formato inválido.",
                VALIDATION_FAILED,
                Map.of("errors", List.of(fieldError(ex.getPropertyName(), "formato inválido"))),
                servletRequest(request));
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    // Parâmetro obrigatório de query ausente

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        recordError(ex, HttpStatus.UNPROCESSABLE_CONTENT, servletRequest(request));

        ProblemDetail body = factory.create(HttpStatus.UNPROCESSABLE_CONTENT,
                "Parâmetro obrigatório da requisição ausente.",
                VALIDATION_FAILED,
                Map.of("errors", List.of(fieldError(ex.getParameterName(), "obrigatório"))),
                servletRequest(request));
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    // Demais erros do Spring MVC (rota inexistente, método não suportado, ResponseStatusException)
    // no mesmo formato, com o código genérico do status

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {
        recordError(ex, statusCode, servletRequest(request));

        ProblemDetail problem = factory.create(statusCode,
                statusCode.value() == HttpStatus.NOT_FOUND.value()
                        ? "Recurso não encontrado." : "A requisição não pôde ser atendida.",
                genericErrorCode(statusCode),
                null,
                servletRequest(request));
        return new ResponseEntity<>(problem, headers, statusCode);
    }

    private static String genericErrorCode(HttpStatusCode status) {
        return switch (status.value()) {
            case 401 -> "UNAUTHENTICATED";
            case 403 -> "FORBIDDEN";
            case 404 -> "NOT_FOUND";
            case 409 -> "CONFLICT";
            case 422 -> VALIDATION_FAILED;
            case 429 -> "RATE_LIMITED";
            case 502, 503 -> "DEPENDENCY_UNAVAILABLE";
            default -> {
                HttpStatus known = HttpStatus.resolve(status.value());
                yield status.is5xxServerError() || known == null ? "INTERNAL_ERROR" : known.name();
            }
        };
    }

    private static Map<String, Object> fieldError(String field, String detail) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("field", field);
        entry.put("errorCode", VALIDATION_FAILED);
        entry.put("detail", detail);
        return entry;
    }

    private HttpServletRequest servletRequest(WebRequest request) {
        return (request instanceof ServletWebRequest swr) ? swr.getRequest() : null;
    }
}
