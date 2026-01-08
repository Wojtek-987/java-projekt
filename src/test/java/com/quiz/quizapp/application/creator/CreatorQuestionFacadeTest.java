package com.quiz.quizapp.application.creator;

import com.quiz.quizapp.domain.entity.QuizEntity;
import com.quiz.quizapp.domain.service.CreatorQuestionService;
import com.quiz.quizapp.domain.service.QuestionReadService;
import com.quiz.quizapp.domain.service.QuizReadService;
import com.quiz.quizapp.web.dto.CreateQuestionForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatorQuestionFacadeTest {

    private static final long QUIZ_ID = 1L;

    @Mock
    private QuizReadService quizReadService;

    @Mock
    private QuestionReadService questionReadService;

    @Mock
    private CreatorQuestionService creatorQuestionService;

    @InjectMocks
    private CreatorQuestionFacade facade;

    @Test
    void getListViewModel_containsExpectedKeys() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizReadService.getOrThrow(QUIZ_ID)).thenReturn(quiz);
        when(questionReadService.listForQuiz(eq(QUIZ_ID), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 200), 0));

        Map<String, Object> vm = facade.getListViewModel(QUIZ_ID);

        assertThat(vm).containsKeys("quiz", "questions", "title", "contentTemplate", "contentFragment");
    }

    @Test
    void getListViewModel_setsQuizReferenceFromReadService() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizReadService.getOrThrow(QUIZ_ID)).thenReturn(quiz);
        when(questionReadService.listForQuiz(eq(QUIZ_ID), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 200), 0));

        Map<String, Object> vm = facade.getListViewModel(QUIZ_ID);

        assertThat(vm.get("quiz")).isSameAs(quiz);
    }

    @Test
    void getListViewModel_setsTitle() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizReadService.getOrThrow(QUIZ_ID)).thenReturn(quiz);
        when(questionReadService.listForQuiz(eq(QUIZ_ID), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 200), 0));

        Map<String, Object> vm = facade.getListViewModel(QUIZ_ID);

        assertThat(vm.get("title")).isEqualTo("Creator • Questions");
    }

    @Test
    void getListViewModel_queriesQuestionsWithFixedPageRequest() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizReadService.getOrThrow(QUIZ_ID)).thenReturn(quiz);
        when(questionReadService.listForQuiz(eq(QUIZ_ID), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 200), 0));

        facade.getListViewModel(QUIZ_ID);

        verify(questionReadService).listForQuiz(QUIZ_ID, PageRequest.of(0, 200));
        verifyNoMoreInteractions(questionReadService);
    }

    @Test
    void getNewFormViewModel_containsExpectedKeys() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizReadService.getOrThrow(2L)).thenReturn(quiz);

        Map<String, Object> vm = facade.getNewFormViewModel(2L);

        assertThat(vm).containsKeys("quiz", "types", "form", "title", "contentTemplate", "contentFragment");
    }

    @Test
    void getNewFormViewModel_setsQuizReferenceFromReadService() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizReadService.getOrThrow(2L)).thenReturn(quiz);

        Map<String, Object> vm = facade.getNewFormViewModel(2L);

        assertThat(vm.get("quiz")).isSameAs(quiz);
    }

    @Test
    void getNewFormViewModel_buildsDefaultFormWithQuizId() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizReadService.getOrThrow(2L)).thenReturn(quiz);

        Map<String, Object> vm = facade.getNewFormViewModel(2L);

        CreateQuestionForm form = (CreateQuestionForm) vm.get("form");
        assertThat(form.getQuizId()).isEqualTo(2L);
    }

    @Test
    void getNewFormViewModel_buildsDefaultFormWithDefaultPoints() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizReadService.getOrThrow(2L)).thenReturn(quiz);

        Map<String, Object> vm = facade.getNewFormViewModel(2L);

        CreateQuestionForm form = (CreateQuestionForm) vm.get("form");
        assertThat(form.getPoints()).isEqualTo(1);
    }

    @Test
    void getNewFormViewModel_buildsDefaultFormWithDefaultType() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizReadService.getOrThrow(2L)).thenReturn(quiz);

        Map<String, Object> vm = facade.getNewFormViewModel(2L);

        CreateQuestionForm form = (CreateQuestionForm) vm.get("form");
        assertThat(form.getType()).isEqualTo("SINGLE_CHOICE");
    }

    @Test
    void getNewFormViewModel_buildsDefaultFormWithDefaultAnswerKeyJsonShape() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizReadService.getOrThrow(2L)).thenReturn(quiz);

        Map<String, Object> vm = facade.getNewFormViewModel(2L);

        CreateQuestionForm form = (CreateQuestionForm) vm.get("form");
        assertThat(form.getAnswerKeyJson()).contains("\"value\"");
    }

    @Test
    void create_delegatesToCreatorQuestionService() {
        CreateQuestionForm form = new CreateQuestionForm();
        form.setType("SHORT_ANSWER");
        form.setPrompt("P");
        form.setPoints(2);
        form.setOptionsJson(null);
        form.setAnswerKeyJson("{\"value\":\"x\"}");

        facade.create(5L, form);

        verify(creatorQuestionService).create(
                eq(5L),
                eq("SHORT_ANSWER"),
                eq("P"),
                eq(2),
                isNull(),
                eq("{\"value\":\"x\"}")
        );
        verifyNoMoreInteractions(creatorQuestionService);
    }

    @Test
    void create_wrapsIllegalArgumentException_asInvalidQuestionJsonException() {
        CreateQuestionForm form = new CreateQuestionForm();
        form.setType("SHORT_ANSWER");
        form.setPrompt("P");
        form.setPoints(2);
        form.setAnswerKeyJson("not-json");

        doThrow(new IllegalArgumentException("bad json"))
                .when(creatorQuestionService)
                .create(anyLong(), anyString(), anyString(), anyInt(), any(), anyString());

        assertThatThrownBy(() -> facade.create(5L, form))
                .isInstanceOf(CreatorQuestionFacade.InvalidQuestionJsonException.class)
                .hasMessageContaining("Invalid JSON");
    }
}
