package com.jihed.kata.tennis.domain.core;

import com.jihed.kata.tennis.domain.core.exception.InvalidGameSequenceException;
import com.jihed.kata.tennis.domain.core.types.GameStatus;
import com.jihed.kata.tennis.domain.core.types.Score;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TennisGameTest {

    @Test
    void shouldStartAtLoveAllAndInProgress() {
        var game = new TennisGame('A', 'B');

        assertEquals(Score.LOVE, game.scoreFirstPlayer());
        assertEquals(Score.LOVE, game.scoreSecondPlayer());
        assertEquals(GameStatus.IN_PROGRESS, game.status());
        assertFalse(game.isFinished());
    }

    @Test
    void shouldIncreaseScoreForPlayerA() {
        var game = play("A");

        assertEquals(Score.FIFTEEN, game.scoreFirstPlayer());
        assertEquals(Score.LOVE, game.scoreSecondPlayer());
        assertEquals(GameStatus.IN_PROGRESS, game.status());
    }

    @Test
    void shouldIncreaseScoreForPlayerB() {
        var game = play("B");

        assertEquals(Score.LOVE, game.scoreFirstPlayer());
        assertEquals(Score.FIFTEEN, game.scoreSecondPlayer());
        assertEquals(GameStatus.IN_PROGRESS, game.status());
    }

    @Test
    void shouldWinForPlayerAWhenLeadingAtForty() {
        var game = play("AAAA");

        assertEquals(GameStatus.WON_BY_FIRST_PLAYER, game.status());
        assertTrue(game.isFinished());
    }

    @Test
    void shouldWinForPlayerBWhenLeadingAtForty() {
        var game = play("BBBB");

        assertEquals(GameStatus.WON_BY_SECOND_PLAYER, game.status());
        assertTrue(game.isFinished());
    }

    @Test
    void shouldReachDeuceAtFortyAll() {
        var game = play("AAABBB");

        assertEquals(GameStatus.DEUCE, game.status());
        assertEquals(Score.FORTY, game.scoreFirstPlayer());
        assertEquals(Score.FORTY, game.scoreSecondPlayer());
    }

    @Test
    void shouldMoveToAdvantageAFromDeuce() {
        var game = play("AAABBBA");

        assertEquals(GameStatus.ADVANTAGE_FIRST_PLAYER, game.status());
    }

    @Test
    void shouldReturnToDeuceWhenOpponentScoresAgainstAdvantage() {
        var game = play("AAABBBAB");

        assertEquals(GameStatus.DEUCE, game.status());
    }

    @Test
    void shouldWinForPlayerAAfterAdvantageA() {
        var game = play("AAABBBAA");

        assertEquals(GameStatus.WON_BY_FIRST_PLAYER, game.status());
        assertTrue(game.isFinished());
    }

    @Test
    void shouldSupportCustomPlayerSymbols() {
        var game = new TennisGame('C', 'D');
        game.awardPoint('C');

        assertEquals(Score.FIFTEEN, game.scoreFirstPlayer());
        assertEquals(Score.LOVE, game.scoreSecondPlayer());
    }

    @Test
    void shouldIgnorePointWhenGameAlreadyFinished() {
        var game = play("AAAA");
        var ex = assertThrows(InvalidGameSequenceException.class, () -> game.awardPoint('B'));

        assertEquals("Invalid sequence: additional points found after the game is already finished.", ex.getMessage());
    }


    private TennisGame play(String sequence) {
        var game = new TennisGame('A', 'B');
        for (char c : sequence.toCharArray()) {
            game.awardPoint(c);
        }
        return game;
    }
}
