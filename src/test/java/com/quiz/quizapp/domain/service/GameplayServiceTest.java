package com.quiz.quizapp.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.quizapp.api.dto.SubmitAnswersRequest;
import com.quiz.quizapp.common.ResourceNotFoundException;
import com.quiz.quizapp.domain.entity.AttemptEntity;
import com.quiz.quizapp.domain.entity.QuestionEntity;
import com.quiz.quizapp.domain.entity.QuizEntity;
import com.quiz.quizapp.domain.repository.AttemptAnswerRepository;
import com.quiz.quizapp.domain.repository.AttemptRepository;
import com.quiz.quizapp.domain.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.quiz.quizapp.api.dto.SubmitAnswerRequest;


import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GameplayServiceTest {

    private AttemptRepository attemptRepository;
    private QuestionRepository questionRepository;
    private AttemptAnswerRepository attemptAnswerRepository;
    private ScoringService scoringService;

    private GameplayService gameplayService;

    @BeforeEach
    void setUp() {
        attemptRepository = mock(AttemptRepository.class);
        questionRepository = mock(QuestionRepository.class);
        attemptAnswerRepository = mock(AttemptAnswerRepository.class);
        scoringService = mock(ScoringService.class);

        gameplayService = new GameplayService(
                attemptRepository,
                questionRepository,
                attemptAnswerRepository,
                scoringService,
                new ObjectMapper()
        );
    }

    @Test
    void submitAndFinish_negativeEnabled_wrongAnswerSubtractsPoints() {
        // given
        QuizEntity quiz = quizWithId(10L);
        quiz.setNegativePointsEnabled(true);

        AttemptEntity attempt = attemptWithIdAndQuiz(100L, quiz);
        // started now to avoid time-limit issues
        setAttemptStartedAt(attempt, OffsetDateTime.now().minusSeconds(1));

        QuestionEntity q = questionWithIdAndQuiz(200L, quiz);
        q.setPoints(5);
        q.setType("SINGLE_CHOICE");
        q.setAnswerKey("{\"value\":\"A\"}");

        when(attemptRepository.findById(100L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(200L)).thenReturn(Optional.of(q));
        when(scoringService.isCorrect(eq(q), anyString())).thenReturn(false);

        SubmitAnswersRequest req = new SubmitAnswersRequest(List.of(
                new SubmitAnswerRequest(200L, "{\"value\":\"B\"}")
        ));

        // when
        var resp = gameplayService.submitAndFinish(100L, req);

        // then
        assertThat(resp.attemptId()).isEqualTo(100L);
        assertThat(resp.totalScore()).isEqualTo(-5);
        assertThat(attempt.getFinishedAt()).isNotNull();

        verify(attemptAnswerRepository, times(1)).save(any());
    }

    @Test
    void submitAndFinish_negativeDisabled_wrongAnswerYieldsZeroPoints() {
        // given
        QuizEntity quiz = quizWithId(10L);
        quiz.setNegativePointsEnabled(false);

        AttemptEntity attempt = attemptWithIdAndQuiz(101L, quiz);
        setAttemptStartedAt(attempt, OffsetDateTime.now().minusSeconds(1));

        QuestionEntity q = questionWithIdAndQuiz(201L, quiz);
        q.setPoints(7);
        q.setType("SINGLE_CHOICE");
        q.setAnswerKey("{\"value\":\"A\"}");

        when(attemptRepository.findById(101L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(201L)).thenReturn(Optional.of(q));
        when(scoringService.isCorrect(eq(q), anyString())).thenReturn(false);

        SubmitAnswersRequest req = new SubmitAnswersRequest(List.of(
                new SubmitAnswerRequest(201L, "{\"value\":\"B\"}")
        ));

        // when
        var resp = gameplayService.submitAndFinish(101L, req);

        // then
        assertThat(resp.totalScore()).isZero();
        verify(attemptAnswerRepository, times(1)).save(any());
    }

    @Test
    void submitAndFinish_timeLimitExceeded_throwsIllegalStateException() {
        // given
        QuizEntity quiz = quizWithId(10L);
        quiz.setTimeLimitSeconds(1);

        AttemptEntity attempt = attemptWithIdAndQuiz(102L, quiz);
        // started long ago => deadline passed
        setAttemptStartedAt(attempt, OffsetDateTime.now().minusSeconds(60));

        when(attemptRepository.findById(102L)).thenReturn(Optional.of(attempt));

        SubmitAnswersRequest req = new SubmitAnswersRequest(List.of(
                new SubmitAnswerRequest(999L, "{\"value\":\"A\"}")
        ));

        // when / then
        assertThatThrownBy(() -> gameplayService.submitAndFinish(102L, req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Time limit exceeded");

        verifyNoInteractions(questionRepository);
        verify(attemptAnswerRepository, never()).save(any());
    }

    @Test
    void submitAndFinish_attemptAlreadyFinished_throwsIllegalStateException() {
        // given
        QuizEntity quiz = quizWithId(10L);
        AttemptEntity attempt = attemptWithIdAndQuiz(103L, quiz);

        // Mark finished
        ReflectionTestUtils.setField(attempt, "finishedAt", OffsetDateTime.now());

        when(attemptRepository.findById(103L)).thenReturn(Optional.of(attempt));

        SubmitAnswersRequest req = new SubmitAnswersRequest(List.of(
                new SubmitAnswerRequest(1L, "{\"value\":\"A\"}")
        ));

        // when / then
        assertThatThrownBy(() -> gameplayService.submitAndFinish(103L, req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Attempt already finished");

        verifyNoInteractions(questionRepository);
        verify(attemptAnswerRepository, never()).save(any());
    }

    @Test
    void submitAndFinish_questionDoesNotBelongToQuiz_throwsIllegalArgumentException() {
        // given
        QuizEntity quiz = quizWithId(10L);

        AttemptEntity attempt = attemptWithIdAndQuiz(104L, quiz);
        setAttemptStartedAt(attempt, OffsetDateTime.now().minusSeconds(1));

        QuizEntity otherQuiz = quizWithId(99L);

        QuestionEntity q = questionWithIdAndQuiz(300L, otherQuiz);
        q.setPoints(3);
        q.setType("SINGLE_CHOICE");
        q.setAnswerKey("{\"value\":\"A\"}");

        when(attemptRepository.findById(104L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(300L)).thenReturn(Optional.of(q));

        SubmitAnswersRequest req = new SubmitAnswersRequest(List.of(
                new SubmitAnswerRequest(300L, "{\"value\":\"A\"}")
        ));

        // when / then
        assertThatThrownBy(() -> gameplayService.submitAndFinish(104L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Question does not belong to quiz");

        verify(attemptAnswerRepository, never()).save(any());
    }

    @Test
    void submitAndFinish_missingAttempt_throwsResourceNotFound() {
        when(attemptRepository.findById(555L)).thenReturn(Optional.empty());

        SubmitAnswersRequest req = new SubmitAnswersRequest(List.of());

        assertThatThrownBy(() -> gameplayService.submitAndFinish(555L, req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Attempt not found: 555");
    }

    // ----------------- helpers -----------------

    private static QuizEntity quizWithId(long id) {
        QuizEntity quiz = new QuizEntity("T", "D");
        ReflectionTestUtils.setField(quiz, "id", id);
        return quiz;
    }

    private static AttemptEntity attemptWithIdAndQuiz(long id, QuizEntity quiz) {
        AttemptEntity attempt = new AttemptEntity(quiz, "Player");
        ReflectionTestUtils.setField(attempt, "id", id);
        return attempt;
    }

    private static void setAttemptStartedAt(AttemptEntity attempt, OffsetDateTime startedAt) {
        ReflectionTestUtils.setField(attempt, "startedAt", startedAt);
    }

    private static QuestionEntity questionWithIdAndQuiz(long id, QuizEntity quiz) {
        QuestionEntity q = new QuestionEntity("SINGLE_CHOICE", "P", 1);
        q.setQuiz(quiz);
        ReflectionTestUtils.setField(q, "id", id);
        return q;
    }
}
