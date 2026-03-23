package com.jihed.kata.tennis.application.usecase;

import com.jihed.kata.tennis.application.command.PlayTennisGameCommand;
import com.jihed.kata.tennis.application.exception.InvalidCommandException;
import com.jihed.kata.tennis.domain.core.TennisGame;
import com.jihed.kata.tennis.domain.core.exception.InvalidGameSequenceException;
import com.jihed.kata.tennis.domain.core.types.GameStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayTennisGameServiceTest {

    private final PlayTennisGameService service = new PlayTennisGameService(TennisGame::new);

    @Test
    void shouldProduceWinningStateForExactWinningSequence() {
        var result = service.play(new PlayTennisGameCommand("ABABAA"));

        assertEquals(6, result.snapshots().size());
        assertEquals(GameStatus.WON_BY_FIRST_PLAYER, result.snapshots().getLast().status());
        assertEquals("A", result.firstPlayerSymbol());
        assertEquals("B", result.secondPlayerSymbol());
    }

    @Test
    void shouldRejectAdditionalPointsAfterGameIsFinished() {
        var ex = assertThrows(InvalidGameSequenceException.class, () -> service.play(new PlayTennisGameCommand("ABABAAB")));

        assertEquals("Invalid sequence: additional points found after the game is already finished.", ex.getMessage());
    }

    @Test
    void shouldProduceDeuceForBalancedSequence() {
        var result = service.play(new PlayTennisGameCommand("AAABBB"));

        assertEquals(GameStatus.DEUCE, result.snapshots().getLast().status());
    }

    @Test
    void shouldRejectNullSequenceAtUseCaseLevel() {
        var ex = assertThrows(InvalidCommandException.class, () -> service.play(new PlayTennisGameCommand(null)));
        assertEquals("Invalid command: 'sequence' must not be null.", ex.getMessage());
    }

    @Test
    void shouldRejectNullCommandAtUseCaseLevel() {
        var ex = assertThrows(InvalidCommandException.class, () -> service.play(null));
        assertEquals("Invalid command: command must not be null.", ex.getMessage());
    }

    @Test
    void shouldAcceptDifferentAlphabetForTwoPlayers() {
        var result = service.play(new PlayTennisGameCommand("CDCDCC"));
        assertEquals(6, result.snapshots().size());
        assertEquals(GameStatus.WON_BY_FIRST_PLAYER, result.snapshots().getLast().status());
        assertEquals("C", result.firstPlayerSymbol());
        assertEquals("D", result.secondPlayerSymbol());
    }

    @Test
    void shouldRejectSequenceContainingMoreThanTwoDistinctPlayers() {
        var ex = assertThrows(InvalidCommandException.class, () -> service.play(new PlayTennisGameCommand("ABC")));
        assertEquals(
                "Invalid command: sequence must contain exactly 2 distinct players. Found 3.",
                ex.getMessage()
        );
    }

    @Test
    void shouldRejectSequenceContainingOnlyOneDistinctPlayer() {
        var ex = assertThrows(InvalidCommandException.class, () -> service.play(new PlayTennisGameCommand("AAAA")));
        assertEquals(
                "Invalid command: sequence must contain exactly 2 distinct players. Found 1.",
                ex.getMessage()
        );
    }
}
