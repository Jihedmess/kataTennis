package com.jihed.kata.tennis.domain.projection;

import com.jihed.kata.tennis.domain.core.types.GameStatus;
import com.jihed.kata.tennis.domain.core.types.Score;

public record GameSnapshot(Score scoreFirstPlayer, Score scoreSecondPlayer, GameStatus status) {
}
