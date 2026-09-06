package com.company.dragonsofmugloar.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameSession;
import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameProgress;
import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameStatus;
import com.company.dragonsofmugloar.exception.AutoplayGameSessionNotFoundException;
import com.company.dragonsofmugloar.service.AutoplayGameService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AutoplayGameController.class)
class AutoplayGameControllerTest {

    private static final String SESSIONS_URL = "/api/v1/autoplay/sessions";
    private static final String SESSION_ID = "6f1c0a3e-8b2d-4c8e-9f1a-2b3c4d5e6f70";
    private static final Instant STARTED = Instant.parse("2026-09-05T14:00:00Z");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AutoplayGameService autoplayGameService;

    @Test
    void startingARunReturns202WithLocation() throws Exception {
        when(autoplayGameService.startGames(3)).thenReturn(AutoplayGameSession.create(SESSION_ID, 3, STARTED));

        mvc.perform(post(SESSIONS_URL).param("games", "3"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.sessionId").value(SESSION_ID))
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.requested").value(3))
                .andExpect(jsonPath("$.finished").value(0))
                .andExpect(jsonPath("$.summary").doesNotExist());
    }

    @Test
    void tooManyGamesIs400() throws Exception {
        mvc.perform(post(SESSIONS_URL).param("games", "21"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(autoplayGameService);
    }

    @Test
    void zeroGamesIs400() throws Exception {
        mvc.perform(post(SESSIONS_URL).param("games", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void progressShowsOutcomesAndSummaryOfFinishedGames() throws Exception {
        AutoplayGameSession session = AutoplayGameSession.create(SESSION_ID, 2, STARTED)
                .withGame(0, AutoplayGameProgress.fromGame(new Game("0NVG7E0r", 0, 87, 3, 1462, 1462, 41), AutoplayGameStatus.FINISHED))
                .withGame(1, AutoplayGameProgress.fromGame(new Game("NmLbq66u", 2, 40, 1, 380, 380, 12), AutoplayGameStatus.RUNNING));
        when(autoplayGameService.getGamesProgress(SESSION_ID)).thenReturn(session);

        mvc.perform(get(SESSIONS_URL + "/" + SESSION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.finished").value(1))
                .andExpect(jsonPath("$.games[0].gameId").value("0NVG7E0r"))
                .andExpect(jsonPath("$.games[0].status").value("FINISHED"))
                .andExpect(jsonPath("$.games[0].score").value(1462))
                .andExpect(jsonPath("$.games[1].status").value("RUNNING"))
                .andExpect(jsonPath("$.summary.min").value(1462))
                .andExpect(jsonPath("$.summary.max").value(1462));
    }

    @Test
    void unknownRunIs404() throws Exception {
        when(autoplayGameService.getGamesProgress(SESSION_ID)).thenThrow(new AutoplayGameSessionNotFoundException(SESSION_ID));

        mvc.perform(get(SESSIONS_URL + "/" + SESSION_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Autoplay session not found: sessionId=" + SESSION_ID));
    }
}
