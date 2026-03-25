package com.jihed.kata.tennis.infrastructure.adapter.in.rest.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record TennisResponse(List<String> results) {
}
