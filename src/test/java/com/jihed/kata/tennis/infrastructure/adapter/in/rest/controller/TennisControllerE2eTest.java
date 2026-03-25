package com.jihed.kata.tennis.infrastructure.adapter.in.rest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TennisControllerE2eTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldProcessRequestEndToEndWithoutMocks() throws Exception {
        mockMvc.perform(post("/api/tennis/play")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sequence\":\"ABABAA\"}"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.results.length()").value(6))
                .andExpect(jsonPath("$.results[0]").value("Player A : 15 / Player B : 0"))
                .andExpect(jsonPath("$.results[1]").value("Player A : 15 / Player B : 15"))
                .andExpect(jsonPath("$.results[2]").value("Player A : 30 / Player B : 15"))
                .andExpect(jsonPath("$.results[3]").value("Player A : 30 / Player B : 30"))
                .andExpect(jsonPath("$.results[4]").value("Player A : 40 / Player B : 30"))
                .andExpect(jsonPath("$.results[5]").value("Player A wins the game"));
    }

    @Test
    void shouldAcceptSequenceWithDifferentTwoLetters() throws Exception {
        mockMvc.perform(post("/api/tennis/play")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sequence\":\"CDCDCC\"}"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.results.length()").value(6))
                .andExpect(jsonPath("$.results[0]").value("Player C : 15 / Player D : 0"))
                .andExpect(jsonPath("$.results[1]").value("Player C : 15 / Player D : 15"))
                .andExpect(jsonPath("$.results[5]").value("Player C wins the game"));
    }

    @Test
    void shouldReturn400ForMalformedJsonPayload() throws Exception {
        mockMvc.perform(post("/api/tennis/play")
                        .header("X-Correlation-Id", "corr-e2e-json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sequence\":\"ABAB\""))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Correlation-Id", "corr-e2e-json"))
                .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request body is invalid or unreadable"))
                .andExpect(jsonPath("$.errors[0].field").value("body"))
                .andExpect(jsonPath("$.errors[0].message").value("Invalid JSON payload"))
                .andExpect(jsonPath("$.path").value("/api/tennis/play"));
    }

    @Test
    void shouldReturn400ForMissingSequenceField() throws Exception {
        mockMvc.perform(post("/api/tennis/play")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].field").value("sequence"))
                .andExpect(jsonPath("$.errors[0].message").value("sequence is required"))
                .andExpect(jsonPath("$.path").value("/api/tennis/play"));
    }

    @Test
    void shouldReturn400ForInvalidSequenceCharacters() throws Exception {
        mockMvc.perform(post("/api/tennis/play")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sequence\":\"AB1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].field").value("sequence"))
                .andExpect(jsonPath("$.errors[0].message", containsString("only letters")))
                .andExpect(jsonPath("$.path").value("/api/tennis/play"));
    }

    @Test
    void shouldReturn400WhenSequenceContainsMoreThanTwoDistinctPlayers() throws Exception {
        mockMvc.perform(post("/api/tennis/play")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sequence\":\"ABC\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.code").value("INVALID_COMMAND"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid command: sequence must contain exactly 2 distinct players. Found 3."))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(0))
                .andExpect(jsonPath("$.path").value("/api/tennis/play"));
    }

    @Test
    void shouldReturn400WhenSequenceContainsOnlyOneDistinctPlayer() throws Exception {
        mockMvc.perform(post("/api/tennis/play")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sequence\":\"AAAA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.code").value("INVALID_COMMAND"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid command: sequence must contain exactly 2 distinct players. Found 1."))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(0))
                .andExpect(jsonPath("$.path").value("/api/tennis/play"));
    }

    @Test
    void shouldReturn400WhenSequenceContainsAdditionalPointsAfterGameFinished() throws Exception {
        mockMvc.perform(post("/api/tennis/play")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sequence\":\"ABABAAB\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.code").value("INVALID_GAME_SEQUENCE"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid sequence: additional points found after the game is already finished."))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].field").value("sequence"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("Invalid sequence: additional points found after the game is already finished."))
                .andExpect(jsonPath("$.path").value("/api/tennis/play"));
    }
}
