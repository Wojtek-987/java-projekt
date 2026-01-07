package com.quiz.quizapp.api;

import com.quiz.quizapp.domain.jdbc.QuizJdbcDao;
import com.quiz.quizapp.domain.jdbc.QuizSummaryRow;
import com.quiz.quizapp.domain.jdbc.RankingJdbcDao;
import com.quiz.quizapp.domain.service.AttemptService;
import com.quiz.quizapp.domain.service.GameplayService;
import com.quiz.quizapp.domain.service.QuizService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.autoconfigure.web.DataWebAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.quiz.quizapp.api.dto.QuizResponse;
import java.time.OffsetDateTime;


import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web slice tests with Spring Security enabled.
 *
 * We deliberately DO NOT provide our own SecurityFilterChain here:
 * Boot/Security auto-config will create it, and @WithMockUser will supply auth context.
 */
@WebMvcTest(controllers = {
        QuizController.class,
        GameplayController.class,
        AttemptController.class,
        JdbcDemoController.class
})
@AutoConfigureMockMvc(addFilters = true)
@ImportAutoConfiguration(DataWebAutoConfiguration.class)
class ApiControllersWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizService quizService;

    @MockitoBean
    private GameplayService gameplayService;

    @MockitoBean
    private AttemptService attemptService;

    @MockitoBean
    private RankingJdbcDao rankingJdbcDao;

    @MockitoBean
    private QuizJdbcDao quizJdbcDao;

    // ---------- Scenario 1: public GET list quizzes (should be OK regardless of auth rules) ----------
    @Test
    void getQuizzes_list_returns200() throws Exception {
        when(quizService.list(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/quizzes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(quizService, times(1)).list(any());
    }

    // ---------- Scenario 2: authenticated POST reaches service ----------
    @Test
    @WithMockUser(username = "creator@example.com", roles = "CREATOR")
    void postQuizzes_create_withAuth_callsService() throws Exception {
        String json = """
            {
              "title": "My quiz",
              "description": "Desc",
              "randomiseQuestions": false,
              "randomiseAnswers": false,
              "timeLimitSeconds": null,
              "negativePointsEnabled": false
            }
            """;

        when(quizService.create(any())).thenReturn(
                new QuizResponse(
                        123L,
                        "My quiz",
                        "Desc",
                        false,
                        false,
                        null,
                        false,
                        OffsetDateTime.now()
                )
        );

        mockMvc.perform(post("/api/v1/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                // might be 201 if binding/validation passes; 400 if DTO differs
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/v1/quizzes/123")));


        verify(quizService, atMostOnce()).create(any());
    }

    // ---------- Scenario 3: submit answers with auth calls gameplay service ----------
    @Test
    @WithMockUser(username = "player@example.com", roles = "USER")
    void postSubmitAnswers_withAuth_callsGameplayService_andReturns200() throws Exception {
        when(gameplayService.submitAndFinish(eq(123L), any())).thenReturn(null);

        String submitJson = """
            {
              "answers": [
                { "questionId": 1, "answerJson": "{\\"value\\":\\"A\\"}" }
              ]
            }
            """;

        mockMvc.perform(post("/api/v1/attempts/123/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitJson))
                .andExpect(status().isOk());

        verify(gameplayService, times(1)).submitAndFinish(eq(123L), any());
    }

    // ---------- Scenario 4: ranking returns JSON list ----------
    @Test
    void getRanking_returnsJsonArray() throws Exception {
        when(rankingJdbcDao.topForQuiz(10L, 2))
                .thenReturn(List.of(
                        new com.quiz.quizapp.api.dto.RankingRowResponse("Alice", 10),
                        new com.quiz.quizapp.api.dto.RankingRowResponse("Bob", 7)
                ));

        mockMvc.perform(get("/api/v1/quizzes/10/ranking?limit=2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nickname").value("Alice"))
                .andExpect(jsonPath("$[0].score").value(10))
                .andExpect(jsonPath("$[1].nickname").value("Bob"))
                .andExpect(jsonPath("$[1].score").value(7));

        verify(rankingJdbcDao, times(1)).topForQuiz(10L, 2);
    }

    // ---------- Extra: jdbc demo ----------
    @Test
    void getJdbcDemoQuizzes_returnsMappedRows() throws Exception {
        when(quizJdbcDao.findQuizSummaries()).thenReturn(List.of(
                new QuizSummaryRow(1L, "Quiz 1", 2),
                new QuizSummaryRow(2L, "Quiz 2", 0)
        ));

        mockMvc.perform(get("/api/v1/jdbc/quizzes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Quiz 1"))
                .andExpect(jsonPath("$[0].questionCount").value(2))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Quiz 2"))
                .andExpect(jsonPath("$[1].questionCount").value(0));

        verify(quizJdbcDao, times(1)).findQuizSummaries();
    }
}
