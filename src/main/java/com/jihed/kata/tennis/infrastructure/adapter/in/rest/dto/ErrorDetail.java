package com.jihed.kata.tennis.infrastructure.adapter.in.rest.dto;

import lombok.Builder;

@Builder
public record ErrorDetail(
        String field,
        String message
) {
}
