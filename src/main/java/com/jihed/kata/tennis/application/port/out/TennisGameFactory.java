package com.jihed.kata.tennis.application.port.out;

import com.jihed.kata.tennis.domain.core.TennisGame;

@FunctionalInterface
public interface TennisGameFactory {
    TennisGame create(char firstPlayerSymbol, char secondPlayerSymbol);
}
