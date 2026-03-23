package com.jihed.kata.tennis.infrastructure.adapter.in.rest.mapper;

import com.jihed.kata.tennis.application.result.PlayTennisGameResult;
import com.jihed.kata.tennis.domain.projection.GameSnapshot;
import com.jihed.kata.tennis.infrastructure.adapter.in.rest.dto.TennisResponse;
import org.springframework.stereotype.Component;

@Component
public class TennisResponseMapper {

    public TennisResponse toResponse(PlayTennisGameResult result) {
        var lines = result.snapshots().stream()
                .map(snapshot -> toDisplayLine(snapshot, result.firstPlayerSymbol(), result.secondPlayerSymbol()))
                .toList();
        return TennisResponse.builder()
                .results(lines)
                .build();
    }

    private String toDisplayLine(GameSnapshot snapshot, String playerASymbol, String playerBSymbol) {
        return switch (snapshot.status()) {
            case DEUCE -> "Deuce";
            case ADVANTAGE_FIRST_PLAYER -> "Advantage Player " + playerASymbol;
            case ADVANTAGE_SECOND_PLAYER -> "Advantage Player " + playerBSymbol;
            case WON_BY_FIRST_PLAYER -> "Player " + playerASymbol + " wins the game";
            case WON_BY_SECOND_PLAYER -> "Player " + playerBSymbol + " wins the game";
            case IN_PROGRESS -> "Player " + playerASymbol + " : " + snapshot.scoreFirstPlayer().display()
                    + " / Player " + playerBSymbol + " : " + snapshot.scoreSecondPlayer().display();
        };
    }
}
