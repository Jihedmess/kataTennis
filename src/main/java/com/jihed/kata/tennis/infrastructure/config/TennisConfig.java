package com.jihed.kata.tennis.infrastructure.config;

import com.jihed.kata.tennis.application.port.in.PlayTennisGameUseCase;
import com.jihed.kata.tennis.application.port.out.TennisGameFactory;
import com.jihed.kata.tennis.application.usecase.PlayTennisGameService;
import com.jihed.kata.tennis.domain.core.TennisGame;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;


@Configuration
public class TennisConfig {
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    TennisGameFactory tennisGameFactory() {
        return TennisGame::new;
    }

    @Bean
    PlayTennisGameUseCase useCase(TennisGameFactory tennisGameFactory) {
        return new PlayTennisGameService(tennisGameFactory);
    }
}
