package com.quiz.quizapp.api;

import com.quiz.quizapp.domain.dto.QuestionForPlayDto;
import com.quiz.quizapp.domain.dto.SubmitOutcome;
import com.quiz.quizapp.domain.service.GameplayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GameplayController.class)
@AutoConfigureMockMvc(addFilters = false)
class GameplayControllerWebMvcTest {

    private static final long ATTEMPT_ID = 5L;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GameplayService gameplayService;

    @Test
    void questions_returns200AndFirstQuestionMetadata() throws Exception {
        when(gameplayService.questionsForAttempt(ATTEMPT_ID)).thenReturn(List.of(
                new QuestionForPlayDto(1L, "SINGLE_CHOICE", "P", 1, "[\"A\",\"B\"]")
        ));

        mvc.perform(get("/api/v1/attempts/5/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].type").value("SINGLE_CHOICE"));

        verify(gameplayService).questionsForAttempt(ATTEMPT_ID);
        verifyNoMoreInteractions(gameplayService);
    }

    @Test
    void submit_returns200AndOutcomeTotals() throws Exception {
        when(gameplayService.submitAndFinish(eq(ATTEMPT_ID), any()))
                .thenReturn(new SubmitOutcome(ATTEMPT_ID, 7));

        mvc.perform(post("/api/v1/attempts/5/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "answers": [
                                    {"questionId": 1, "answerJson": "{\\"value\\":\\"A\\"}"}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId").value(5))
                .andExpect(jsonPath("$.totalScore").value(7));

        verify(gameplayService).submitAndFinish(eq(ATTEMPT_ID), any());
        verifyNoMoreInteractions(gameplayService);
    }
}
