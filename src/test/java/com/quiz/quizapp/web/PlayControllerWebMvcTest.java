package com.quiz.quizapp.web;

import com.quiz.quizapp.application.play.PlayFacade;
import com.quiz.quizapp.domain.entity.AttemptEntity;
import com.quiz.quizapp.domain.entity.QuizEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = PlayController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.thymeleaf.check-template-location=false"
})
@Import(PlayControllerWebMvcTest.NoThymeleafViews.class)
class PlayControllerWebMvcTest {

    @TestConfiguration
    static class NoThymeleafViews {

        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        ViewResolver testViewResolver() {
            return new ViewResolver() {
                @Override
                public View resolveViewName(String viewName, Locale locale) {
                    return (model, request, response) -> response.setStatus(200);
                }
            };
        }
    }

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PlayFacade playFacade;

    @Test
    void home_rendersPlayHomeViewName() throws Exception {
        when(playFacade.listPlayableQuizzes()).thenReturn(List.of());

        mvc.perform(get("/play"))
                .andExpect(status().isOk())
                .andExpect(view().name("play/home"));

        verify(playFacade).listPlayableQuizzes();
        verifyNoMoreInteractions(playFacade);
    }

    @Test
    void home_exposesQuizzesModelAttribute() throws Exception {
        when(playFacade.listPlayableQuizzes()).thenReturn(List.of());

        mvc.perform(get("/play"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("quizzes"));

        verify(playFacade).listPlayableQuizzes();
        verifyNoMoreInteractions(playFacade);
    }

    @Test
    void startForm_rendersStartViewName() throws Exception {
        Map<String, Object> vm = new HashMap<>();
        vm.put("quiz", new QuizEntity("T", "D"));
        when(playFacade.getStartViewModel(1L)).thenReturn(vm);

        mvc.perform(get("/play/quizzes/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("play/start"));

        verify(playFacade).getStartViewModel(1L);
        verifyNoMoreInteractions(playFacade);
    }

    @Test
    void startForm_exposesQuizModelAttribute() throws Exception {
        Map<String, Object> vm = new HashMap<>();
        vm.put("quiz", new QuizEntity("T", "D"));
        when(playFacade.getStartViewModel(1L)).thenReturn(vm);

        mvc.perform(get("/play/quizzes/1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("quiz"));

        verify(playFacade).getStartViewModel(1L);
        verifyNoMoreInteractions(playFacade);
    }

    @Test
    void start_redirectsToAttemptUrl() throws Exception {
        when(playFacade.startAttempt(1L, "nick")).thenReturn(99L);

        mvc.perform(post("/play/quizzes/1/start").param("nickname", "nick"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/play/attempts/99"));

        verify(playFacade).startAttempt(1L, "nick");
        verifyNoMoreInteractions(playFacade);
    }

    @Test
    void attempt_rendersAttemptViewName() throws Exception {
        QuizEntity quiz = new QuizEntity("T", "D");
        AttemptEntity attempt = new AttemptEntity(quiz, "nick");

        Map<String, Object> vm = new HashMap<>();
        vm.put("attempt", attempt);
        vm.put("quiz", quiz);
        vm.put("questions", List.of());
        when(playFacade.getAttemptViewModel(5L)).thenReturn(vm);

        mvc.perform(get("/play/attempts/5"))
                .andExpect(status().isOk())
                .andExpect(view().name("play/attempt"));

        verify(playFacade).getAttemptViewModel(5L);
        verifyNoMoreInteractions(playFacade);
    }

    @Test
    void attempt_exposesAttemptQuizQuestionsModelAttributes() throws Exception {
        QuizEntity quiz = new QuizEntity("T", "D");
        AttemptEntity attempt = new AttemptEntity(quiz, "nick");

        Map<String, Object> vm = new HashMap<>();
        vm.put("attempt", attempt);
        vm.put("quiz", quiz);
        vm.put("questions", List.of());
        when(playFacade.getAttemptViewModel(5L)).thenReturn(vm);

        mvc.perform(get("/play/attempts/5"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("attempt", "quiz", "questions"));

        verify(playFacade).getAttemptViewModel(5L);
        verifyNoMoreInteractions(playFacade);
    }

    @Test
    void submit_success_redirectsToRankingUrl() throws Exception {
        when(playFacade.submitAttempt(eq(5L), any()))
                .thenReturn(new PlayFacade.SubmitOutcome(1L, 42));

        mvc.perform(post("/play/attempts/5/submit").param("q_1", "A"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/play/quizzes/1/ranking"));

        verify(playFacade).submitAttempt(eq(5L), any());
        verifyNoMoreInteractions(playFacade);
    }

    @Test
    void submit_success_setsFlashLastScore() throws Exception {
        when(playFacade.submitAttempt(eq(5L), any()))
                .thenReturn(new PlayFacade.SubmitOutcome(1L, 42));

        mvc.perform(post("/play/attempts/5/submit").param("q_1", "A"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("lastScore", 42));

        verify(playFacade).submitAttempt(eq(5L), any());
        verifyNoMoreInteractions(playFacade);
    }

    @Test
    void submit_incompleteAnswers_redirectsToRoot() throws Exception {
        when(playFacade.submitAttempt(eq(5L), any()))
                .thenThrow(new PlayFacade.IncompleteAnswersException("You must answer every question."));

        mvc.perform(post("/play/attempts/5/submit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(playFacade).submitAttempt(eq(5L), any());
        verifyNoMoreInteractions(playFacade);
    }

    @Test
    void ranking_rendersRankingViewName() throws Exception {
        Map<String, Object> vm = Map.of(
                "quiz", new QuizEntity("T", "D"),
                "rows", List.of()
        );
        when(playFacade.getRankingViewModel(1L, 10)).thenReturn(vm);

        mvc.perform(get("/play/quizzes/1/ranking").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("play/ranking"));

        verify(playFacade).getRankingViewModel(1L, 10);
        verifyNoMoreInteractions(playFacade);
    }

    @Test
    void ranking_exposesQuizAndRowsModelAttributes() throws Exception {
        Map<String, Object> vm = Map.of(
                "quiz", new QuizEntity("T", "D"),
                "rows", List.of()
        );
        when(playFacade.getRankingViewModel(1L, 10)).thenReturn(vm);

        mvc.perform(get("/play/quizzes/1/ranking").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("quiz", "rows"));

        verify(playFacade).getRankingViewModel(1L, 10);
        verifyNoMoreInteractions(playFacade);
    }
}
