package com.jihed.kata.tennis.application.port.in;

import com.jihed.kata.tennis.application.command.PlayTennisGameCommand;
import com.jihed.kata.tennis.application.result.PlayTennisGameResult;

public interface PlayTennisGameUseCase {
    PlayTennisGameResult play(PlayTennisGameCommand command);
}
