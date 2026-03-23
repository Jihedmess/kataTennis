package com.jihed.kata.tennis.application.usecase;

import com.jihed.kata.tennis.application.command.PlayTennisGameCommand;
import com.jihed.kata.tennis.application.exception.InvalidCommandException;
import com.jihed.kata.tennis.application.model.NormalizedGameInput;
import com.jihed.kata.tennis.application.port.in.PlayTennisGameUseCase;
import com.jihed.kata.tennis.application.port.out.TennisGameFactory;
import com.jihed.kata.tennis.application.result.PlayTennisGameResult;
import com.jihed.kata.tennis.domain.projection.GameSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PlayTennisGameService implements PlayTennisGameUseCase {
    private static final int REQUIRED_PLAYER_COUNT = 2;

    private final TennisGameFactory tennisGameFactory;

    public PlayTennisGameService(TennisGameFactory tennisGameFactory) {
        this.tennisGameFactory = Objects.requireNonNull(tennisGameFactory, "tennisGameFactory must not be null");
    }

    @Override
    public PlayTennisGameResult play(PlayTennisGameCommand command) {
        if (command == null) {
            throw new InvalidCommandException("Invalid command: command must not be null.");
        }
        var sequence = command.sequence();
        if (sequence == null) {
            throw new InvalidCommandException("Invalid command: 'sequence' must not be null.");
        }
        var normalizedGameInput = normalizeSequenceForGame(sequence);

        var game = tennisGameFactory.create(
                normalizedGameInput.firstPlayerSymbol(),
                normalizedGameInput.secondPlayerSymbol()
        );
        var snapshots = new ArrayList<GameSnapshot>();

        for (char c : normalizedGameInput.points().toCharArray()) {
            game.awardPoint(c);
            snapshots.add(game.snapshot());
        }

        return new PlayTennisGameResult(
                List.copyOf(snapshots),
                String.valueOf(normalizedGameInput.firstPlayerSymbol()),
                String.valueOf(normalizedGameInput.secondPlayerSymbol())
        );
    }

    private NormalizedGameInput normalizeSequenceForGame(String sequence) {
        var normalizedInput = sequence.toUpperCase(Locale.ROOT);
        var distinctSymbols = new LinkedHashSet<Character>();

        for (char symbol : normalizedInput.toCharArray()) {
            distinctSymbols.add(symbol);
        }

        if (distinctSymbols.size() != REQUIRED_PLAYER_COUNT) {
            throw invalidPlayerCount(distinctSymbols.size());
        }

        var playerSymbols = distinctSymbols.stream().toList();
        var firstPlayerSymbol = playerSymbols.get(0);
        var secondPlayerSymbol = playerSymbols.get(1);

        return new NormalizedGameInput(normalizedInput, firstPlayerSymbol, secondPlayerSymbol);
    }

    private InvalidCommandException invalidPlayerCount(int distinctPlayers) {
        return new InvalidCommandException(
                "Invalid command: sequence must contain exactly " + REQUIRED_PLAYER_COUNT
                        + " distinct players. Found " + distinctPlayers + "."
        );
    }

}