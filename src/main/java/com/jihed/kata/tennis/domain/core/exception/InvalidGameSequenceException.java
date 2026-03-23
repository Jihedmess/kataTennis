package com.jihed.kata.tennis.domain.core.exception;

public class InvalidGameSequenceException extends RuntimeException {

    public InvalidGameSequenceException(String message) {
        super(message);
    }
}
