package com.quiz.quizapp.domain.service;

import com.quiz.quizapp.common.ResourceNotFoundException;
import com.quiz.quizapp.domain.entity.AttemptEntity;
import com.quiz.quizapp.domain.entity.QuizEntity;
import com.quiz.quizapp.domain.repository.AttemptRepository;
import com.quiz.quizapp.domain.repository.QuizRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttemptServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private AttemptRepository attemptRepository;

    @InjectMocks
    private AttemptService service;

    @Test
    void start_trimsNicknameBeforePersisting() {
        var quiz = new QuizEntity("Q", "d");
        setId(quiz, 1L);
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

        when(attemptRepository.save(any(AttemptEntity.class))).thenAnswer(inv -> {
            AttemptEntity a = inv.getArgument(0);
            setId(a, 123L);
            return a;
        });

        service.start(1L, "  nick  ");

        ArgumentCaptor<AttemptEntity> captor = ArgumentCaptor.forClass(AttemptEntity.class);
        verify(attemptRepository).save(captor.capture());
        assertThat(captor.getValue().getNickname()).isEqualTo("nick");
    }

    @Test
    void start_returnsAttemptIdFromSavedEntity() {
        var quiz = new QuizEntity("Q", "d");
        setId(quiz, 1L);
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

        when(attemptRepository.save(any(AttemptEntity.class))).thenAnswer(inv -> {
            AttemptEntity a = inv.getArgument(0);
            setId(a, 123L);
            return a;
        });

        var info = service.start(1L, "nick");

        assertThat(info.id()).isEqualTo(123L);
    }

    @Test
    void start_returnsQuizIdFromLoadedQuiz() {
        var quiz = new QuizEntity("Q", "d");
        setId(quiz, 1L);
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

        when(attemptRepository.save(any(AttemptEntity.class))).thenAnswer(inv -> {
            AttemptEntity a = inv.getArgument(0);
            setId(a, 123L);
            return a;
        });

        var info = service.start(1L, "nick");

        assertThat(info.quizId()).isEqualTo(1L);
    }

    @Test
    void start_throwsWhenQuizMissing() {
        when(quizRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.start(99L, "x"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Quiz not found");

        verify(attemptRepository, never()).save(any());
    }

    @Test
    void finish_setsScoreOnAttempt() {
        var quiz = new QuizEntity("Q", "d");
        setId(quiz, 1L);

        var attempt = new AttemptEntity(quiz, "nick");
        setId(attempt, 5L);

        when(attemptRepository.findById(5L)).thenReturn(Optional.of(attempt));

        var info = service.finish(5L, 123);

        assertThat(info.score()).isEqualTo(123);
    }

    @Test
    void finish_setsFinishedAtTimestamp() {
        var quiz = new QuizEntity("Q", "d");
        setId(quiz, 1L);

        var attempt = new AttemptEntity(quiz, "nick");
        setId(attempt, 5L);

        when(attemptRepository.findById(5L)).thenReturn(Optional.of(attempt));

        var info = service.finish(5L, 123);

        assertThat(info.finishedAt()).isNotNull();
    }

    @Test
    void finish_returnsAttemptId() {
        var quiz = new QuizEntity("Q", "d");
        setId(quiz, 1L);

        var attempt = new AttemptEntity(quiz, "nick");
        setId(attempt, 5L);

        when(attemptRepository.findById(5L)).thenReturn(Optional.of(attempt));

        var info = service.finish(5L, 123);

        assertThat(info.id()).isEqualTo(5L);
    }

    private static void setId(Object entity, Long id) {
        try {
            Field f = entity.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id via reflection for " + entity.getClass(), e);
        }
    }
}
