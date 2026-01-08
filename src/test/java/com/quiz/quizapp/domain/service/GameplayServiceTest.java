package com.quiz.quizapp.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.quizapp.common.ResourceNotFoundException;
import com.quiz.quizapp.domain.dto.SubmitAnswerDto;
import com.quiz.quizapp.domain.dto.SubmitAnswersCommand;
import com.quiz.quizapp.domain.entity.AttemptEntity;
import com.quiz.quizapp.domain.entity.QuestionEntity;
import com.quiz.quizapp.domain.entity.QuizEntity;
import com.quiz.quizapp.domain.repository.AttemptAnswerRepository;
import com.quiz.quizapp.domain.repository.AttemptRepository;
import com.quiz.quizapp.domain.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameplayServiceTest {

    @Mock
    private AttemptRepository attemptRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AttemptAnswerRepository attemptAnswerRepository;

    @Mock
    private ScoringService scoringService;

    private GameplayService gameplayService;

    @BeforeEach
    void setUp() {
        gameplayService = new GameplayService(
                attemptRepository,
                questionRepository,
                attemptAnswerRepository,
                scoringService,
                new ObjectMapper()
        );
    }

    @Test
    void questionsForAttempt_throwsWhenAttemptMissing() {
        when(attemptRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameplayService.questionsForAttempt(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Attempt not found");

        verify(attemptRepository).findById(1L);
        verifyNoMoreInteractions(attemptRepository, questionRepository, attemptAnswerRepository, scoringService);
    }

    @Test
    void questionsForAttempt_throwsWhenAttemptAlreadyFinished() {
        var quiz = quiz(false);
        var attempt = attempt(quiz);
        setFinishedAt(attempt, OffsetDateTime.now());

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> gameplayService.questionsForAttempt(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already finished");

        verify(attemptRepository).findById(1L);
        verifyNoMoreInteractions(attemptRepository, questionRepository, attemptAnswerRepository, scoringService);
    }

    @Test
    void questionsForAttempt_randomisesWhenEnabled_preservesElements() {
        var quiz = quiz(true);
        var attempt = attempt(quiz);

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findByQuiz_Id(eq(quiz.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(
                        question(quiz, 1L),
                        question(quiz, 2L),
                        question(quiz, 3L)
                )));

        var out = gameplayService.questionsForAttempt(1L);

        assertThat(out).extracting(q -> q.id()).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void submitAndFinish_throwsWhenTimeLimitExceeded() {
        var quiz = quiz(false);
        quiz.setTimeLimitSeconds(10);

        var attempt = attempt(quiz);
        setStartedAt(attempt, OffsetDateTime.now().minusSeconds(60));

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> gameplayService.submitAndFinish(1L, new SubmitAnswersCommand(List.of())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Time limit exceeded");

        verify(attemptRepository).findById(1L);
        verifyNoMoreInteractions(attemptRepository, questionRepository, attemptAnswerRepository, scoringService);
    }

    @Test
    void submitAndFinish_throwsWhenAttemptAlreadyFinished() {
        var quiz = quiz(false);
        var attempt = attempt(quiz);
        setFinishedAt(attempt, OffsetDateTime.now());

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> gameplayService.submitAndFinish(1L, new SubmitAnswersCommand(List.of())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already finished");

        verify(attemptRepository).findById(1L);
        verifyNoMoreInteractions(attemptRepository, questionRepository, attemptAnswerRepository, scoringService);
    }

    @Test
    void submitAndFinish_throwsWhenQuestionDoesNotBelongToQuiz() {
        var quiz = quiz(false);
        var attempt = attempt(quiz);

        var otherQuiz = quiz(false);
        setId(otherQuiz, 999L);

        long foreignQuestionId = 44L;
        var foreignQuestion = question(otherQuiz, foreignQuestionId);

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(foreignQuestionId)).thenReturn(Optional.of(foreignQuestion));

        var req = new SubmitAnswersCommand(List.of(new SubmitAnswerDto(foreignQuestionId, "{\"value\":\"x\"}")));

        assertThatThrownBy(() -> gameplayService.submitAndFinish(1L, req))
                .isInstanceOf(IllegalArgumentException.class);

        verify(attemptRepository).findById(1L);
        verify(questionRepository).findById(foreignQuestionId);
        verifyNoMoreInteractions(attemptRepository, questionRepository, attemptAnswerRepository, scoringService);
    }

    @Test
    void submitAndFinish_awardsNegativePointsWhenEnabled_totalScoreIsNegative() {
        var quiz = quiz(false);
        quiz.setNegativePointsEnabled(true);

        var attempt = attempt(quiz);

        var q1 = question(quiz, 11L);
        q1.setPoints(5);

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(11L)).thenReturn(Optional.of(q1));
        when(scoringService.isCorrect(eq(q1), anyString())).thenReturn(false);

        var req = new SubmitAnswersCommand(List.of(new SubmitAnswerDto(11L, "{\"value\":\"x\"}")));
        var out = gameplayService.submitAndFinish(1L, req);

        assertThat(out.totalScore()).isEqualTo(-5);
    }

    @Test
    void submitAndFinish_awardsNegativePointsWhenEnabled_setsAttemptFinishedAt() {
        var quiz = quiz(false);
        quiz.setNegativePointsEnabled(true);

        var attempt = attempt(quiz);

        var q1 = question(quiz, 11L);
        q1.setPoints(5);

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(11L)).thenReturn(Optional.of(q1));
        when(scoringService.isCorrect(eq(q1), anyString())).thenReturn(false);

        gameplayService.submitAndFinish(1L, new SubmitAnswersCommand(List.of(new SubmitAnswerDto(11L, "{\"value\":\"x\"}"))));

        assertThat(attempt.getFinishedAt()).isNotNull();
    }

    @Test
    void submitAndFinish_awardsNegativePointsWhenEnabled_savesAttemptAnswer() {
        var quiz = quiz(false);
        quiz.setNegativePointsEnabled(true);

        var attempt = attempt(quiz);

        var q1 = question(quiz, 11L);
        q1.setPoints(5);

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(11L)).thenReturn(Optional.of(q1));
        when(scoringService.isCorrect(eq(q1), anyString())).thenReturn(false);

        gameplayService.submitAndFinish(1L, new SubmitAnswersCommand(List.of(new SubmitAnswerDto(11L, "{\"value\":\"x\"}"))));

        verify(attemptAnswerRepository).save(argThat(a -> a != null));
    }

    @Test
    void submitAndFinish_awardsZeroWhenIncorrectAndNegativeDisabled() {
        var quiz = quiz(false);
        quiz.setNegativePointsEnabled(false);

        var attempt = attempt(quiz);

        var q1 = question(quiz, 11L);
        q1.setPoints(5);

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(11L)).thenReturn(Optional.of(q1));
        when(scoringService.isCorrect(eq(q1), anyString())).thenReturn(false);

        var out = gameplayService.submitAndFinish(1L, new SubmitAnswersCommand(List.of(new SubmitAnswerDto(11L, "{\"value\":\"x\"}"))));

        assertThat(out.totalScore()).isEqualTo(0);
    }

    private static QuizEntity quiz(boolean randomiseQuestions) {
        var q = new QuizEntity("T", "D");
        setId(q, 1L);
        q.setRandomiseQuestions(randomiseQuestions);
        q.setNegativePointsEnabled(false);
        q.setRandomiseAnswers(false);
        q.setTimeLimitSeconds(null);
        return q;
    }

    private static AttemptEntity attempt(QuizEntity quiz) {
        var a = new AttemptEntity(quiz, "nick");
        setId(a, 1L);
        setStartedAt(a, OffsetDateTime.now());
        return a;
    }

    private static QuestionEntity question(QuizEntity quiz, long id) {
        var q = new QuestionEntity();
        setId(q, id);
        q.setQuiz(quiz);
        q.setType("SINGLE_CHOICE");
        q.setPrompt("P");
        q.setPoints(1);
        q.setAnswerKey("{\"value\":\"A\"}");
        return q;
    }

    private static void setId(Object entity, Long id) {
        try {
            Field f = entity.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setStartedAt(AttemptEntity attempt, OffsetDateTime startedAt) {
        try {
            Field f = AttemptEntity.class.getDeclaredField("startedAt");
            f.setAccessible(true);
            f.set(attempt, startedAt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setFinishedAt(AttemptEntity attempt, OffsetDateTime finishedAt) {
        try {
            Field f = AttemptEntity.class.getDeclaredField("finishedAt");
            f.setAccessible(true);
            f.set(attempt, finishedAt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
