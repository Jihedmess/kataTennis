package com.jihed.kata.tennis.infrastructure.adapter.in.rest.exception;

import com.jihed.kata.tennis.application.exception.InvalidCommandException;
import com.jihed.kata.tennis.domain.core.exception.InvalidGameSequenceException;
import com.jihed.kata.tennis.infrastructure.adapter.in.rest.common.CorrelationIdConstants;
import com.jihed.kata.tennis.infrastructure.adapter.in.rest.dto.ErrorDetail;
import com.jihed.kata.tennis.infrastructure.adapter.in.rest.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;

@RestControllerAdvice
public class TennisExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(TennisExceptionHandler.class);
    private static final List<ErrorDetail> NO_ERRORS = List.of();
    private final Clock clock;

    public TennisExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var detailsByField = new LinkedHashMap<String, ErrorDetail>();
        for (var error : ex.getBindingResult().getFieldErrors()) {
            var detail = ErrorDetail.builder()
                    .field(error.getField())
                    .message(error.getDefaultMessage())
                    .build();
            detailsByField.putIfAbsent(error.getField(), detail);
            if (isRequiredConstraint(error.getCode())) {
                detailsByField.put(error.getField(), detail);
            }
        }
        var details = List.copyOf(detailsByField.values());
        log.warn("Bean validation failed path={}", request.getRequestURI());
        log.debug("Bean validation details path={} details={}", request.getRequestURI(), details);
        return ResponseEntity.badRequest().body(buildError(
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                details,
                request
        ));
    }

    private boolean isRequiredConstraint(String code) {
        return "NotNull".equals(code) || "NotBlank".equals(code) || "NotEmpty".equals(code);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON body path={}", request.getRequestURI());
        log.debug("Malformed JSON technical details path={}", request.getRequestURI(), ex);
        return ResponseEntity.badRequest().body(buildError(
                "MALFORMED_JSON",
                HttpStatus.BAD_REQUEST,
                "Request body is invalid or unreadable",
                List.of(ErrorDetail.builder().field("body").message("Invalid JSON payload").build()),
                request
        ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        log.warn("HTTP method not supported path={}", request.getRequestURI());
        log.debug("HTTP method not supported details path={} method={} supported={}",
                request.getRequestURI(), ex.getMethod(), ex.getSupportedHttpMethods());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(buildError(
                "METHOD_NOT_ALLOWED",
                HttpStatus.METHOD_NOT_ALLOWED,
                "HTTP method is not supported for this endpoint",
                NO_ERRORS,
                request
        ));
    }

    @ExceptionHandler(InvalidGameSequenceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSequence(InvalidGameSequenceException ex, HttpServletRequest request) {
        var details = List.of(
                ErrorDetail.builder()
                        .field("sequence")
                        .message(ex.getMessage())
                        .build()
        );
        log.warn("Invalid game sequence path={}", request.getRequestURI());
        log.debug("Invalid game sequence details path={}", request.getRequestURI(), ex);
        return ResponseEntity.badRequest().body(buildError(
                "INVALID_GAME_SEQUENCE",
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                details,
                request
        ));
    }

    @ExceptionHandler(InvalidCommandException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCommand(InvalidCommandException ex, HttpServletRequest request) {
        log.warn("Invalid command path={}", request.getRequestURI());
        log.debug("Invalid command details path={}", request.getRequestURI(), ex);
        return ResponseEntity.badRequest().body(buildError(
                "INVALID_COMMAND",
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                NO_ERRORS,
                request
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error path={}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildError(
                "INTERNAL_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected server error. Retry later or contact support with the correlationId.",
                NO_ERRORS,
                request
        ));
    }

    private ErrorResponse buildError(
            String code,
            HttpStatus status,
            String message,
            List<ErrorDetail> errors,
            HttpServletRequest request
    ) {
        return ErrorResponse.builder()
                .code(code)
                .status(status.value())
                .message(message)
                .errors(errors)
                .path(request.getRequestURI())
                .timestamp(Instant.now(clock))
                .correlationId(resolveCorrelationId(request))
                .build();
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        var attribute = request.getAttribute(CorrelationIdConstants.ATTRIBUTE);
        if (attribute instanceof String id && !id.isBlank()) {
            return id;
        }

        var incomingId = request.getHeader(CorrelationIdConstants.HEADER);
        if (incomingId != null && !incomingId.isBlank()) {
            return incomingId;
        }

        return "N/A";
    }
}
