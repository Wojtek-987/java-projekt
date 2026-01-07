package com.quiz.quizapp.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.quizapp.common.ResourceNotFoundException;
import com.quiz.quizapp.domain.entity.QuestionEntity;
import com.quiz.quizapp.domain.entity.QuizEntity;
import com.quiz.quizapp.domain.repository.QuestionRepository;
import com.quiz.quizapp.domain.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreatorQuestionServiceTest {

    private QuizRepository quizRepository;
    private QuestionRepository questionRepository;

    private CreatorQuestionService service;

    @BeforeEach
    void setUp() {
        quizRepository = mock(QuizRepository.class);
        questionRepository = mock(QuestionRepository.class);
        service = new CreatorQuestionService(quizRepository, questionRepository, new ObjectMapper());
    }

    @Test
    void create_missingQuiz_throwsResourceNotFound() {
        when(quizRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(
                1L, "single_choice", "P", 1,
                "{\"options\":[]}", "{\"value\":\"A\"}"
        )).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Quiz not found: 1");

        verifyNoInteractions(questionRepository);
    }

    @Test
    void create_invalidAnswerKeyJson_throwsIllegalArgumentException() {
        QuizEntity quiz = new QuizEntity("T", "D");
        ReflectionTestUtils.setField(quiz, "id", 1L);
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

        assertThatThrownBy(() -> service.create(
                1L, "single_choice", "P", 1,
                null, "{not-json"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid JSON");

        verifyNoInteractions(questionRepository);
    }

    @Test
    void create_validInput_savesQuestionWithNormalisedFields() {
        QuizEntity quiz = new QuizEntity("T", "D");
        ReflectionTestUtils.setField(quiz, "id", 1L);
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

        service.create(
                1L,
                "  single_choice ",
                "  What is 2+2? ",
                5,
                "   ", // blank => should become null
                "{\"value\":\"4\"}"
        );

        ArgumentCaptor<QuestionEntity> captor = ArgumentCaptor.forClass(QuestionEntity.class);
        verify(questionRepository, times(1)).save(captor.capture());

        QuestionEntity saved = captor.getValue();
        assertThat(saved.getQuiz()).isSameAs(quiz);
        assertThat(saved.getType()).isEqualTo("SINGLE_CHOICE");
        assertThat(saved.getPrompt()).isEqualTo("What is 2+2?");
        assertThat(saved.getPoints()).isEqualTo(5);
        assertThat(saved.getOptions()).isNull();
        assertThat(saved.getAnswerKey()).isEqualTo("{\"value\":\"4\"}");
    }
}
