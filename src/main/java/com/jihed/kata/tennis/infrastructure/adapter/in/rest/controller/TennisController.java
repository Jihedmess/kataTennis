package com.jihed.kata.tennis.infrastructure.adapter.in.rest.controller;

import com.jihed.kata.tennis.application.command.PlayTennisGameCommand;
import com.jihed.kata.tennis.application.port.in.PlayTennisGameUseCase;
import com.jihed.kata.tennis.infrastructure.adapter.in.rest.dto.TennisRequest;
import com.jihed.kata.tennis.infrastructure.adapter.in.rest.dto.TennisResponse;
import com.jihed.kata.tennis.infrastructure.adapter.in.rest.mapper.TennisResponseMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tennis")
public class TennisController {

    private static final Logger log = LoggerFactory.getLogger(TennisController.class);
    private final PlayTennisGameUseCase useCase;
    private final TennisResponseMapper responseMapper;

    public TennisController(PlayTennisGameUseCase useCase, TennisResponseMapper responseMapper) {
        this.useCase = useCase;
        this.responseMapper = responseMapper;
    }

    @PostMapping("/play")
    public TennisResponse play(@Valid @RequestBody TennisRequest request) {
        var command = new PlayTennisGameCommand(request.sequence());
        log.info("Play tennis request received sequenceLength={}", command.sequence().length());
        var result = useCase.play(command);
        log.info("Play tennis request processed snapshots={}", result.snapshots().size());
        return responseMapper.toResponse(result);
    }
}
