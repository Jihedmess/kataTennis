package com.jihed.kata.tennis.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record TennisRequest(
        @NotBlank(message = "sequence is required")
        @Pattern(regexp = "[a-zA-Z]+", message = "sequence must contain only letters")
        String sequence
) {
}
