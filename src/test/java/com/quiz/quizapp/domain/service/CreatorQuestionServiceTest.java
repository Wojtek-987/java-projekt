package com.quiz.quizapp.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.quizapp.common.ResourceNotFoundException;
import com.quiz.quizapp.domain.entity.QuestionEntity;
import com.quiz.quizapp.domain.entity.QuizEntity;
import com.quiz.quizapp.domain.repository.QuestionRepository;
import com.quiz.quizapp.domain.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatorQuestionServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    private CreatorQuestionService service;

    @BeforeEach
    void setUp() {
        service = new CreatorQuestionService(quizRepository, questionRepository, new ObjectMapper());
    }

    @Test
    void create_throwsNotFound_whenQuizMissing() {
        when(quizRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(
                1L, "SHORT_ANSWER", "P", 1, null, "{\"value\":\"x\"}"
        )).isInstanceOf(ResourceNotFoundException.class);

        verify(questionRepository, never()).save(any());
    }

    @Test
    void create_setsQuizOnSavedQuestion() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizRepository.findById(5L)).thenReturn(Optional.of(quiz));

        service.create(5L, "SHORT_ANSWER", "P", 3, null, "{\"value\":\"x\"}");

        ArgumentCaptor<QuestionEntity> captor = ArgumentCaptor.forClass(QuestionEntity.class);
        verify(questionRepository).save(captor.capture());
        assertThat(captor.getValue().getQuiz()).isSameAs(quiz);
    }

    @Test
    void create_trimsAndUppercasesType() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizRepository.findById(5L)).thenReturn(Optional.of(quiz));

        service.create(5L, "  short_answer  ", "P", 3, null, "{\"value\":\"x\"}");

        ArgumentCaptor<QuestionEntity> captor = ArgumentCaptor.forClass(QuestionEntity.class);
        verify(questionRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo("SHORT_ANSWER");
    }

    @Test
    void create_trimsPrompt() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizRepository.findById(5L)).thenReturn(Optional.of(quiz));

        service.create(5L, "SHORT_ANSWER", "  hello world  ", 3, null, "{\"value\":\"x\"}");

        ArgumentCaptor<QuestionEntity> captor = ArgumentCaptor.forClass(QuestionEntity.class);
        verify(questionRepository).save(captor.capture());
        assertThat(captor.getValue().getPrompt()).isEqualTo("hello world");
    }

    @Test
    void create_setsPoints() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizRepository.findById(5L)).thenReturn(Optional.of(quiz));

        service.create(5L, "SHORT_ANSWER", "P", 3, null, "{\"value\":\"x\"}");

        ArgumentCaptor<QuestionEntity> captor = ArgumentCaptor.forClass(QuestionEntity.class);
        verify(questionRepository).save(captor.capture());
        assertThat(captor.getValue().getPoints()).isEqualTo(3);
    }

    @Test
    void create_convertsBlankOptionsToNull() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizRepository.findById(5L)).thenReturn(Optional.of(quiz));

        service.create(5L, "SHORT_ANSWER", "P", 3, "   ", "{\"value\":\"x\"}");

        ArgumentCaptor<QuestionEntity> captor = ArgumentCaptor.forClass(QuestionEntity.class);
        verify(questionRepository).save(captor.capture());
        assertThat(captor.getValue().getOptions()).isNull();
    }

    @Test
    void create_trimsAnswerKeyJson() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizRepository.findById(5L)).thenReturn(Optional.of(quiz));

        service.create(5L, "SHORT_ANSWER", "P", 3, null, " { \"value\" : \"x\" } ");

        ArgumentCaptor<QuestionEntity> captor = ArgumentCaptor.forClass(QuestionEntity.class);
        verify(questionRepository).save(captor.capture());
        assertThat(captor.getValue().getAnswerKey()).isEqualTo("{ \"value\" : \"x\" }");
    }

    @Test
    void create_rejectsInvalidAnswerKeyJson() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizRepository.findById(5L)).thenReturn(Optional.of(quiz));

        assertThatThrownBy(() -> service.create(
                5L,
                "SHORT_ANSWER",
                "P",
                1,
                null,
                "not-json"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid JSON");

        verify(questionRepository, never()).save(any());
    }

    @Test
    void create_rejectsInvalidOptionsJson_whenProvided() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizRepository.findById(5L)).thenReturn(Optional.of(quiz));

        assertThatThrownBy(() -> service.create(
                5L,
                "SINGLE_CHOICE",
                "P",
                1,
                "not-json",
                "{\"value\":\"A\"}"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid JSON");

        verify(questionRepository, never()).save(any());
    }
}
