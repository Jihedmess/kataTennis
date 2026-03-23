package com.jihed.kata.tennis.domain.core;

import com.jihed.kata.tennis.domain.core.types.GameStatus;
import com.jihed.kata.tennis.domain.core.types.Score;
import com.jihed.kata.tennis.domain.core.exception.InvalidGameSequenceException;
import com.jihed.kata.tennis.domain.core.exception.InvalidGameStateException;
import com.jihed.kata.tennis.domain.projection.GameSnapshot;

public class TennisGame {
    private final char playerOneSymbol;
    private final char playerTwoSymbol;
    private Score scoreFirstPlayer = Score.LOVE;
    private Score scoreSecondPlayer = Score.LOVE;
    private GameStatus status = GameStatus.IN_PROGRESS;

    public TennisGame(char playerOneSymbol, char playerTwoSymbol) {
        validatePlayerSymbols(playerOneSymbol, playerTwoSymbol);
        this.playerOneSymbol = playerOneSymbol;
        this.playerTwoSymbol = playerTwoSymbol;
    }

    public Score scoreFirstPlayer() { return scoreFirstPlayer; }
    public Score scoreSecondPlayer() { return scoreSecondPlayer; }
    public GameStatus status() { return status; }

    public boolean isFinished() {
        return status == GameStatus.WON_BY_FIRST_PLAYER || status == GameStatus.WON_BY_SECOND_PLAYER;
    }

    public void awardPoint(char winner) {
        validateWinner(winner);
        if (isFinished()) {
            throw new InvalidGameSequenceException(
                    "Invalid sequence: additional points found after the game is already finished."
            );
        }

        switch (status) {
            case IN_PROGRESS -> handlePointInProgress(winner);
            case DEUCE -> moveToAdvantage(winner);
            case ADVANTAGE_FIRST_PLAYER -> handlePointAtAdvantage(winner, playerOneSymbol);
            case ADVANTAGE_SECOND_PLAYER -> handlePointAtAdvantage(winner, playerTwoSymbol);
            default -> throw new InvalidGameStateException("Invalid game status: " + status);
        }
    }

    public GameSnapshot snapshot() {
        return new GameSnapshot(scoreFirstPlayer(), scoreSecondPlayer(), status);
    }

    private void handlePointInProgress(char winner) {
        if (isWinningPoint(winner)) {
            winBy(winner);
            return;
        }

        incrementScore(winner);
        if (isFortyAll()) {
            moveToDeuce();
        }
    }

    private void moveToAdvantage(char winner) {
        status = isFirstPlayer(winner) ? GameStatus.ADVANTAGE_FIRST_PLAYER : GameStatus.ADVANTAGE_SECOND_PLAYER;
    }

    private void handlePointAtAdvantage(char winner, char advantagedPlayer) {
        if (winner == advantagedPlayer) {
            winBy(winner);
            return;
        }

        moveToDeuce();
    }

    private boolean isWinningPoint(char winner) {
        var winnerIsFirstPlayer = isFirstPlayer(winner);
        var winnerScore = winnerIsFirstPlayer ? scoreFirstPlayer : scoreSecondPlayer;
        var opponentScore = winnerIsFirstPlayer ? scoreSecondPlayer : scoreFirstPlayer;
        return winnerScore == Score.FORTY && opponentScore != Score.FORTY;
    }

    private void incrementScore(char winner) {
        if (isFirstPlayer(winner)) {
            scoreFirstPlayer = scoreFirstPlayer.next();
        } else {
            scoreSecondPlayer = scoreSecondPlayer.next();
        }
    }

    private boolean isFortyAll() {
        return scoreFirstPlayer == Score.FORTY && scoreSecondPlayer == Score.FORTY;
    }

    private void winBy(char winner) {
        status = isFirstPlayer(winner) ? GameStatus.WON_BY_FIRST_PLAYER : GameStatus.WON_BY_SECOND_PLAYER;
    }

    private void moveToDeuce() {
        status = GameStatus.DEUCE;
    }

    private void validateWinner(char winner) {
        if (winner != playerOneSymbol && winner != playerTwoSymbol) {
            throw invalidWinner(winner);
        }
    }

    private boolean isFirstPlayer(char winner) {
        if (winner == playerOneSymbol) {
            return true;
        }
        if (winner == playerTwoSymbol) {
            return false;
        }
        throw invalidWinner(winner);
    }

    private void validatePlayerSymbols(char firstPlayerSymbol, char secondPlayerSymbol) {
        if (firstPlayerSymbol == secondPlayerSymbol) {
            throw new IllegalArgumentException("Players must use distinct symbols.");
        }
    }

    private InvalidGameSequenceException invalidWinner(char winner) {
        return new InvalidGameSequenceException(
                "Invalid point winner '" + winner + "'. Allowed values are '"
                        + playerOneSymbol + "' or '" + playerTwoSymbol + "'."
        );
    }


}
