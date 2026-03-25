package com.jihed.kata.tennis.domain.core.types;

public enum Score {
    LOVE("0"),
    FIFTEEN("15"),
    THIRTY("30"),
    FORTY("40");

    private final String display;

    Score(String display) {
        this.display = display;
    }

    public String display() {
        return display;
    }

    public Score next() {
        return switch (this) {
            case LOVE -> FIFTEEN;
            case FIFTEEN -> THIRTY;
            case THIRTY -> FORTY;
            case FORTY -> FORTY;
        };
    }
}
