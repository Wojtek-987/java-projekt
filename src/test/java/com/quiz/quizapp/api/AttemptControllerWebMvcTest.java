package com.quiz.quizapp.api;

import com.quiz.quizapp.api.dto.RankingRowResponse;
import com.quiz.quizapp.domain.dto.AttemptInfo;
import com.quiz.quizapp.domain.service.AttemptService;
import com.quiz.quizapp.domain.service.RankingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AttemptController.class)
@AutoConfigureMockMvc(addFilters = false)
class AttemptControllerWebMvcTest {

    private static final long QUIZ_ID = 1L;
    private static final long ATTEMPT_ID = 9L;
    private static final String NICKNAME = "nick";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AttemptService attemptService;

    @MockitoBean
    private RankingService rankingService;

    @Test
    void start_returns200AndAttemptIdentifiers() throws Exception {
        when(attemptService.start(eq(QUIZ_ID), eq(NICKNAME)))
                .thenReturn(new AttemptInfo(ATTEMPT_ID, QUIZ_ID, NICKNAME, 0, OffsetDateTime.now(), null));

        mvc.perform(post("/api/v1/quizzes/1/attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"nick\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.quizId").value(1));

        verify(attemptService).start(QUIZ_ID, NICKNAME);
        verifyNoMoreInteractions(attemptService, rankingService);
    }

    @Test
    void finish_returns200AndScore() throws Exception {
        when(attemptService.finish(eq(ATTEMPT_ID), eq(10)))
                .thenReturn(new AttemptInfo(ATTEMPT_ID, QUIZ_ID, NICKNAME, 10, OffsetDateTime.now(), OffsetDateTime.now()));

        mvc.perform(post("/api/v1/attempts/9/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(10));

        verify(attemptService).finish(ATTEMPT_ID, 10);
        verifyNoMoreInteractions(attemptService, rankingService);
    }

    @Test
    void ranking_returns200AndTopRow() throws Exception {
        when(rankingService.topForQuiz(QUIZ_ID, 10))
                .thenReturn(List.of(new RankingRowResponse(NICKNAME, 5)));

        mvc.perform(get("/api/v1/quizzes/1/ranking?limit=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nickname").value("nick"))
                .andExpect(jsonPath("$[0].score").value(5));

        verify(rankingService).topForQuiz(QUIZ_ID, 10);
        verifyNoMoreInteractions(attemptService, rankingService);
    }
}
