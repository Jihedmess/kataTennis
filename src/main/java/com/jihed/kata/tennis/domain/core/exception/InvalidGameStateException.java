package com.jihed.kata.tennis.domain.core.exception;

public class InvalidGameStateException extends RuntimeException {

    public InvalidGameStateException(String message) {
        super(message);
    }
}
