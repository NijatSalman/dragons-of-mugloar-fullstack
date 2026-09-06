package com.company.dragonsofmugloar.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.constraints.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = ApiExceptionHandlerTest.ThrowingController.class)
@Import(ApiExceptionHandlerTest.ThrowingController.class)
class ApiExceptionHandlerTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void traceIdIsInTheMdcAsMicrometerWouldPutIt() {
        MDC.put("traceId", TRACE_ID);
    }

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void unknownGameIs404WithTraceId() throws Exception {
        mockMvc.perform(get("/throw/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Game not found: gameId=nope1234"))
                .andExpect(jsonPath("$.traceId").value(TRACE_ID));
    }

    @Test
    void finishedGameIs410() throws Exception {
        mockMvc.perform(get("/throw/game-over"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.detail").value("Game over: gameId=ggLmesXI"));
    }

    @Test
    void rejectedRequestIs409() throws Exception {
        mockMvc.perform(get("/throw/rejected"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Ad not available: gameId=ggLmesXI, adId=HiCtYxHC"));
    }

    @Test
    void unavailableGameServerIs502WithoutInternals() throws Exception {
        mockMvc.perform(get("/throw/unavailable"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.detail").value("Game server is currently unavailable"));
    }

    @Test
    void unexpectedErrorIs500WithoutInternals() throws Exception {
        mockMvc.perform(get("/throw/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Unexpected error"));
    }

    @Test
    void invalidPathVariableIs400() throws Exception {
        mockMvc.perform(get("/throw/validated/not valid!"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownPathIs404() throws Exception {
        mockMvc.perform(get("/no/such/path"))
                .andExpect(status().isNotFound());
    }

    @RestController
    static class ThrowingController {

        @GetMapping("/throw/not-found")
        void notFound() {
            throw new GameNotFoundException("nope1234");
        }

        @GetMapping("/throw/game-over")
        void gameOver() {
            throw new GameOverException("ggLmesXI");
        }

        @GetMapping("/throw/rejected")
        void rejected() {
            throw new AdNotAvailableException("ggLmesXI", "HiCtYxHC");
        }

        @GetMapping("/throw/unavailable")
        void unavailable() {
            throw new GameApiException("Game server failed: gameId=ggLmesXI, status=503");
        }

        @GetMapping("/throw/unexpected")
        void unexpected() {
            throw new IllegalStateException("boom");
        }

        @GetMapping("/throw/validated/{gameId}")
        String validated(@PathVariable @Pattern(regexp = "[A-Za-z0-9]{1,64}") String gameId) {
            return gameId;
        }
    }
}
