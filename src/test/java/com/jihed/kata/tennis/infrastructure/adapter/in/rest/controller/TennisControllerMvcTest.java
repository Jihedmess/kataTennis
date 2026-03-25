package com.jihed.kata.tennis.infrastructure.adapter.in.rest.controller;

import com.jihed.kata.tennis.application.port.in.PlayTennisGameUseCase;
import com.jihed.kata.tennis.application.result.PlayTennisGameResult;
import com.jihed.kata.tennis.infrastructure.adapter.in.rest.dto.TennisResponse;
import com.jihed.kata.tennis.infrastructure.adapter.in.rest.exception.TennisExceptionHandler;
import com.jihed.kata.tennis.infrastructure.adapter.in.rest.filter.CorrelationIdFilter;
import com.jihed.kata.tennis.infrastructure.adapter.in.rest.mapper.TennisResponseMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TennisController.class)
@Import({TennisExceptionHandler.class, CorrelationIdFilter.class, TennisControllerMvcTest.ClockTestConfig.class})
class TennisControllerMvcTest {

    @TestConfiguration
    static class ClockTestConfig {
        @Bean
        Clock clock() {
            return Clock.systemUTC();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayTennisGameUseCase useCase;

    @MockitoBean
    private TennisResponseMapper mapper;

    @Test
    void shouldReturn400WhenSequenceIsBlank() throws Exception {
        mockMvc.perform(post("/api/tennis/play")
                        .header("X-Correlation-Id", "corr-blank-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sequence\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("X-Correlation-Id", "corr-blank-123"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[*].field", hasItem("sequence")))
                .andExpect(jsonPath("$.errors[*].message", hasItem("sequence is required")))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.path").value("/api/tennis/play"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.correlationId").value("corr-blank-123"));
    }

    @Test
    void shouldReturn400WhenSequenceContainsNonLetterCharacters() throws Exception {
        mockMvc.perform(post("/api/tennis/play")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sequence\":\"AB1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[*].field", hasItem("sequence")))
                .andExpect(jsonPath("$.errors[*].message", hasItem("sequence must contain only letters")))
                .andExpect(jsonPath("$.path").value("/api/tennis/play"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.correlationId").isString());
    }

    @Test
    void shouldReturn400WhenJsonPayloadIsMalformed() throws Exception {
        mockMvc.perform(post("/api/tennis/play")
                        .header("X-Correlation-Id", "corr-json-400")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sequence\":\"ABAB\""))
                .andExpect(status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("X-Correlation-Id", "corr-json-400"))
                .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request body is invalid or unreadable"))
                .andExpect(jsonPath("$.errors[0].field").value("body"))
                .andExpect(jsonPath("$.errors[0].message").value("Invalid JSON payload"))
                .andExpect(jsonPath("$.path").value("/api/tennis/play"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.correlationId").value("corr-json-400"));
    }

    @Test
    void shouldReturn200ForValidRequest() throws Exception {
        when(useCase.play(any())).thenReturn(new PlayTennisGameResult(List.of(), "A", "B"));
        when(mapper.toResponse(any())).thenReturn(
                TennisResponse.builder()
                        .results(List.of("Player A : 15 / Player B : 0"))
                        .build()
        );

        mockMvc.perform(post("/api/tennis/play")
                        .header("X-Correlation-Id", "corr-ok-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sequence\":\"ABAB\"}"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("X-Correlation-Id", "corr-ok-999"))
                .andExpect(jsonPath("$.results[0]").value("Player A : 15 / Player B : 0"));
    }

    @Test
    void shouldReturn500WithStandardErrorContract() throws Exception {
        when(useCase.play(any())).thenThrow(new RuntimeException("DB timeout"));

        mockMvc.perform(post("/api/tennis/play")
                        .header("X-Correlation-Id", "corr-500-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sequence\":\"ABAB\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("X-Correlation-Id", "corr-500-001"))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected server error. Retry later or contact support with the correlationId."))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(0))
                .andExpect(jsonPath("$.path").value("/api/tennis/play"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.correlationId").value("corr-500-001"));
    }

    @Test
    void shouldKeepProvidedCorrelationIdHeaderAsIs() throws Exception {
        mockMvc.perform(post("/api/tennis/play")
                        .header("X-Correlation-Id", "bad id!")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sequence\":\"AB1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("X-Correlation-Id", "bad id!"))
                .andExpect(jsonPath("$.correlationId").value("bad id!"));
    }

    @Test
    void shouldGenerateCorrelationIdWhenIncomingIdIsTooLong() throws Exception {
        var tooLongCorrelationId = "x".repeat(200);

        mockMvc.perform(post("/api/tennis/play")
                        .header("X-Correlation-Id", tooLongCorrelationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sequence\":\"AB1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .exists("X-Correlation-Id"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("X-Correlation-Id", not(tooLongCorrelationId)))
                .andExpect(jsonPath("$.correlationId").isString())
                .andExpect(jsonPath("$.correlationId").value(not(tooLongCorrelationId)));
    }

    @Test
    void shouldReturn405WhenHttpMethodIsNotSupported() throws Exception {
        mockMvc.perform(get("/api/tennis/play")
                        .header("X-Correlation-Id", "corr-405-001"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("X-Correlation-Id", "corr-405-001"))
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.message").value("HTTP method is not supported for this endpoint"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(0))
                .andExpect(jsonPath("$.path").value("/api/tennis/play"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.correlationId").value("corr-405-001"));
    }

}
