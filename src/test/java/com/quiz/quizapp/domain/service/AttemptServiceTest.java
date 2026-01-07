package com.quiz.quizapp.domain.service;

import com.quiz.quizapp.api.dto.FinishAttemptRequest;
import com.quiz.quizapp.api.dto.StartAttemptRequest;
import com.quiz.quizapp.common.ResourceNotFoundException;
import com.quiz.quizapp.domain.entity.AttemptEntity;
import com.quiz.quizapp.domain.entity.QuizEntity;
import com.quiz.quizapp.domain.repository.AttemptRepository;
import com.quiz.quizapp.domain.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AttemptServiceTest {

    private QuizRepository quizRepository;
    private AttemptRepository attemptRepository;

    private AttemptService attemptService;

    @BeforeEach
    void setUp() {
        quizRepository = mock(QuizRepository.class);
        attemptRepository = mock(AttemptRepository.class);
        attemptService = new AttemptService(quizRepository, attemptRepository);
    }

    @Test
    void start_trimsNickname_andReturnsResponse() {
        QuizEntity quiz = new QuizEntity("Title", "Desc");
        ReflectionTestUtils.setField(quiz, "id", 10L);

        when(quizRepository.findById(10L)).thenReturn(Optional.of(quiz));

        // Return the same entity, but with an ID as if persisted
        when(attemptRepository.save(any(AttemptEntity.class))).thenAnswer(inv -> {
            AttemptEntity a = inv.getArgument(0);
            ReflectionTestUtils.setField(a, "id", 123L);
            return a;
        });

        var resp = attemptService.start(10L, new StartAttemptRequest("  Wojtek  "));

        assertThat(resp.id()).isEqualTo(123L);
        assertThat(resp.quizId()).isEqualTo(10L);
        assertThat(resp.nickname()).isEqualTo("Wojtek");
        assertThat(resp.score()).isZero();
        assertThat(resp.finishedAt()).isNull();
    }

    @Test
    void start_missingQuiz_throwsResourceNotFound() {
        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attemptService.start(999L, new StartAttemptRequest("X")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Quiz not found: 999");
    }

    @Test
    void finish_setsScoreAndFinishedAt() {
        QuizEntity quiz = new QuizEntity("Title", "Desc");
        ReflectionTestUtils.setField(quiz, "id", 10L);

        AttemptEntity attempt = new AttemptEntity(quiz, "Player");
        ReflectionTestUtils.setField(attempt, "id", 50L);

        when(attemptRepository.findById(50L)).thenReturn(Optional.of(attempt));

        var resp = attemptService.finish(50L, new FinishAttemptRequest(42));

        assertThat(resp.id()).isEqualTo(50L);
        assertThat(resp.score()).isEqualTo(42);
        assertThat(resp.finishedAt()).isNotNull();
    }

    @Test
    void finish_missingAttempt_throwsResourceNotFound() {
        when(attemptRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attemptService.finish(404L, new FinishAttemptRequest(0)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Attempt not found: 404");
    }
}
