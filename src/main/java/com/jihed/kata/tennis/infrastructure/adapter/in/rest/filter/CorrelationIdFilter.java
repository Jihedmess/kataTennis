package com.jihed.kata.tennis.infrastructure.adapter.in.rest.filter;

import com.jihed.kata.tennis.infrastructure.adapter.in.rest.common.CorrelationIdConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final int MAX_CORRELATION_ID_LENGTH = 128;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        var correlationId = resolveCorrelationId(request);
        var startNanos = System.nanoTime();

        request.setAttribute(CorrelationIdConstants.ATTRIBUTE, correlationId);
        response.setHeader(CorrelationIdConstants.HEADER, correlationId);
        MDC.put(CorrelationIdConstants.MDC_KEY, correlationId);
        log.info("HTTP request started method={} path={}", request.getMethod(), request.getRequestURI());
        try {
            filterChain.doFilter(request, response);
        } finally {
            logRequestCompleted(request, response, startNanos);
            MDC.remove(CorrelationIdConstants.MDC_KEY);
        }
    }

    private void logRequestCompleted(HttpServletRequest request, HttpServletResponse response, long startNanos) {
        var durationMs = (System.nanoTime() - startNanos) / 1_000_000;
        var status = response.getStatus();
        var method = request.getMethod();
        var path = request.getRequestURI();
        switch (status / 100) {
            case 5 -> log.error(
                    "HTTP request completed with error method={} path={} status={} durationMs={}",
                    method, path, status, durationMs
            );
            case 4 -> log.warn(
                    "HTTP request completed with client error method={} path={} status={} durationMs={}",
                    method, path, status, durationMs
            );
            default -> log.info(
                    "HTTP request completed method={} path={} status={} durationMs={}",
                    method, path, status, durationMs
            );
        }
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        var incomingId = request.getHeader(CorrelationIdConstants.HEADER);
        if (incomingId == null) {
            return UUID.randomUUID().toString();
        }

        var sanitizedId = incomingId
                .trim()
                .replace("\r", "")
                .replace("\n", "");

        if (sanitizedId.isBlank() || sanitizedId.length() > MAX_CORRELATION_ID_LENGTH) {
            return UUID.randomUUID().toString();
        }

        return sanitizedId;
    }
}
