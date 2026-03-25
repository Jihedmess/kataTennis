package com.jihed.kata.tennis.application.result;

import com.jihed.kata.tennis.domain.projection.GameSnapshot;

import java.util.List;
import java.util.Objects;

public record PlayTennisGameResult(List<GameSnapshot> snapshots, String firstPlayerSymbol, String secondPlayerSymbol) {

    public PlayTennisGameResult {
        snapshots = List.copyOf(Objects.requireNonNull(snapshots, "snapshots must not be null"));
        firstPlayerSymbol = requireSymbol(firstPlayerSymbol, "firstPlayerSymbol");
        secondPlayerSymbol = requireSymbol(secondPlayerSymbol, "secondPlayerSymbol");
        if (firstPlayerSymbol.equals(secondPlayerSymbol)) {
            throw new IllegalArgumentException("Player symbols must be distinct.");
        }
    }

    private static String requireSymbol(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank.");
        }
        return value;
    }
}
