package com.quiz.quizapp.api;

import com.quiz.quizapp.domain.dto.QuizCreateCommand;
import com.quiz.quizapp.domain.dto.QuizInfo;
import com.quiz.quizapp.domain.dto.QuizUpdateCommand;
import com.quiz.quizapp.domain.service.QuizService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = QuizController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuizControllerWebMvcTest {

    private static final long QUIZ_ID = 1L;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private QuizService quizService;

    @Test
    void list_returns200AndFirstRowIdentifiers() throws Exception {
        var info = new QuizInfo(QUIZ_ID, "T", "D", false, false, null, false, OffsetDateTime.now());
        when(quizService.list(any()))
                .thenReturn(new PageImpl<>(List.of(info), PageRequest.of(0, 20), 1));

        mvc.perform(get("/api/v1/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("T"));

        verify(quizService).list(any());
        verifyNoMoreInteractions(quizService);
    }

    @Test
    void get_returns200AndTitle() throws Exception {
        var info = new QuizInfo(QUIZ_ID, "T", "D", false, false, null, false, OffsetDateTime.now());
        when(quizService.get(QUIZ_ID)).thenReturn(info);

        mvc.perform(get("/api/v1/quizzes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("T"));

        verify(quizService).get(QUIZ_ID);
        verifyNoMoreInteractions(quizService);
    }

    @Test
    void create_returns201AndLocationHeader() throws Exception {
        var created = new QuizInfo(10L, "T", "D", true, true, 30, true, OffsetDateTime.now());
        when(quizService.create(any(QuizCreateCommand.class))).thenReturn(created);

        mvc.perform(post("/api/v1/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"T",
                                  "description":"D",
                                  "randomiseQuestions":true,
                                  "randomiseAnswers":true,
                                  "timeLimitSeconds":30,
                                  "negativePointsEnabled":true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.containsString("/api/v1/quizzes/10")));

        verify(quizService).create(any(QuizCreateCommand.class));
        verifyNoMoreInteractions(quizService);
    }

    @Test
    void create_returns201AndResponseBodyId() throws Exception {
        var created = new QuizInfo(10L, "T", "D", true, true, 30, true, OffsetDateTime.now());
        when(quizService.create(any(QuizCreateCommand.class))).thenReturn(created);

        mvc.perform(post("/api/v1/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"T",
                                  "description":"D",
                                  "randomiseQuestions":true,
                                  "randomiseAnswers":true,
                                  "timeLimitSeconds":30,
                                  "negativePointsEnabled":true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));

        verify(quizService).create(any(QuizCreateCommand.class));
        verifyNoMoreInteractions(quizService);
    }

    @Test
    void update_returns200AndUpdatedTitle() throws Exception {
        var updated = new QuizInfo(QUIZ_ID, "T2", "D2", false, true, null, false, OffsetDateTime.now());
        when(quizService.update(eq(QUIZ_ID), any(QuizUpdateCommand.class))).thenReturn(updated);

        mvc.perform(put("/api/v1/quizzes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"T2",
                                  "description":"D2",
                                  "randomiseQuestions":false,
                                  "randomiseAnswers":true,
                                  "timeLimitSeconds":null,
                                  "negativePointsEnabled":false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("T2"));

        verify(quizService).update(eq(QUIZ_ID), any(QuizUpdateCommand.class));
        verifyNoMoreInteractions(quizService);
    }

    @Test
    void delete_returns204() throws Exception {
        mvc.perform(delete("/api/v1/quizzes/1"))
                .andExpect(status().isNoContent());
    }
}
