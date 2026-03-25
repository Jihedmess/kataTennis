package com.jihed.kata.tennis.infrastructure.adapter.in.rest.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record ErrorResponse(
        String code,
        int status,
        String message,
        List<ErrorDetail> errors,
        String path,
        Instant timestamp,
        String correlationId
) {
}


