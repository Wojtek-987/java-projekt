package com.quiz.quizapp.domain.service;

import com.quiz.quizapp.common.ResourceNotFoundException;
import com.quiz.quizapp.domain.dto.QuizCreateCommand;
import com.quiz.quizapp.domain.dto.QuizInfo;
import com.quiz.quizapp.domain.dto.QuizUpdateCommand;
import com.quiz.quizapp.domain.entity.QuizEntity;
import com.quiz.quizapp.domain.repository.QuizRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @InjectMocks
    private QuizService quizService;

    @Test
    void list_mapsEntityIdsToInfo() {
        QuizEntity q1 = new QuizEntity("T1", "D1");
        setId(q1, 1L);
        setCreatedAt(q1, OffsetDateTime.now().minusDays(1));

        QuizEntity q2 = new QuizEntity("T2", "D2");
        setId(q2, 2L);
        setCreatedAt(q2, OffsetDateTime.now());

        Page<QuizEntity> page = new PageImpl<>(List.of(q1, q2), PageRequest.of(0, 10), 2);
        when(quizRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        Page<QuizInfo> out = quizService.list(PageRequest.of(0, 10));

        assertThat(out.getContent()).extracting(QuizInfo::id).containsExactly(1L, 2L);
    }

    @Test
    void list_mapsTitlesToInfo() {
        QuizEntity q1 = new QuizEntity("T1", "D1");
        setId(q1, 1L);
        setCreatedAt(q1, OffsetDateTime.now().minusDays(1));

        QuizEntity q2 = new QuizEntity("T2", "D2");
        setId(q2, 2L);
        setCreatedAt(q2, OffsetDateTime.now());

        Page<QuizEntity> page = new PageImpl<>(List.of(q1, q2), PageRequest.of(0, 10), 2);
        when(quizRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        Page<QuizInfo> out = quizService.list(PageRequest.of(0, 10));

        assertThat(out.getContent()).extracting(QuizInfo::title).containsExactly("T1", "T2");
    }

    @Test
    void list_delegatesToRepositoryWithPageable() {
        Page<QuizEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(quizRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        quizService.list(PageRequest.of(0, 10));

        verify(quizRepository).findAll(PageRequest.of(0, 10));
        verifyNoMoreInteractions(quizRepository);
    }

    @Test
    void get_throwsNotFound_whenMissing() {
        when(quizRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.get(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Quiz not found: 99");
    }

    @Test
    void get_returnsId() {
        QuizEntity q = new QuizEntity("T", "D");
        setId(q, 5L);
        setCreatedAt(q, OffsetDateTime.now());
        when(quizRepository.findById(5L)).thenReturn(Optional.of(q));

        QuizInfo out = quizService.get(5L);

        assertThat(out.id()).isEqualTo(5L);
    }

    @Test
    void get_returnsTitle() {
        QuizEntity q = new QuizEntity("T", "D");
        setId(q, 5L);
        setCreatedAt(q, OffsetDateTime.now());
        when(quizRepository.findById(5L)).thenReturn(Optional.of(q));

        QuizInfo out = quizService.get(5L);

        assertThat(out.title()).isEqualTo("T");
    }

    @Test
    void get_mapsSettingsFlagsAndTimeLimit() {
        QuizEntity q = new QuizEntity("T", "D");
        q.setRandomiseQuestions(true);
        q.setRandomiseAnswers(true);
        q.setTimeLimitSeconds(12);
        q.setNegativePointsEnabled(true);
        setId(q, 5L);
        setCreatedAt(q, OffsetDateTime.now());
        when(quizRepository.findById(5L)).thenReturn(Optional.of(q));

        QuizInfo out = quizService.get(5L);

        assertThat(out.randomiseQuestions()).isTrue();
        assertThat(out.randomiseAnswers()).isTrue();
        assertThat(out.timeLimitSeconds()).isEqualTo(12);
        assertThat(out.negativePointsEnabled()).isTrue();
    }

    @Test
    void create_returnsSavedId() {
        QuizCreateCommand cmd = new QuizCreateCommand("T", "D", true, false, 30, true);

        when(quizRepository.save(any(QuizEntity.class))).thenAnswer(inv -> {
            QuizEntity saved = inv.getArgument(0);
            setId(saved, 10L);
            setCreatedAt(saved, OffsetDateTime.now());
            return saved;
        });

        QuizInfo out = quizService.create(cmd);

        assertThat(out.id()).isEqualTo(10L);
    }

    @Test
    void create_persistsFieldsFromCommand() {
        QuizCreateCommand cmd = new QuizCreateCommand("T", "D", true, false, 30, true);

        when(quizRepository.save(any(QuizEntity.class))).thenAnswer(inv -> {
            QuizEntity saved = inv.getArgument(0);
            setId(saved, 10L);
            setCreatedAt(saved, OffsetDateTime.now());
            return saved;
        });

        quizService.create(cmd);

        ArgumentCaptor<QuizEntity> captor = ArgumentCaptor.forClass(QuizEntity.class);
        verify(quizRepository).save(captor.capture());

        QuizEntity saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("T");
        assertThat(saved.getDescription()).isEqualTo("D");
        assertThat(saved.isRandomiseQuestions()).isTrue();
        assertThat(saved.isRandomiseAnswers()).isFalse();
        assertThat(saved.getTimeLimitSeconds()).isEqualTo(30);
        assertThat(saved.isNegativePointsEnabled()).isTrue();
    }

    @Test
    void update_throwsNotFound_whenMissing() {
        when(quizRepository.findById(1L)).thenReturn(Optional.empty());

        QuizUpdateCommand cmd = new QuizUpdateCommand("T", "D", false, false, null, false);

        assertThatThrownBy(() -> quizService.update(1L, cmd))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Quiz not found: 1");
    }

    @Test
    void update_updatesEntityTitle() {
        QuizEntity existing = new QuizEntity("OLD", "OLD-D");
        setId(existing, 3L);
        setCreatedAt(existing, OffsetDateTime.now().minusDays(2));

        when(quizRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(quizRepository.save(any(QuizEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        QuizUpdateCommand cmd = new QuizUpdateCommand("NEW", "NEW-D", true, true, 15, true);

        quizService.update(3L, cmd);

        assertThat(existing.getTitle()).isEqualTo("NEW");
    }

    @Test
    void update_returnsMappedInfo() {
        QuizEntity existing = new QuizEntity("OLD", "OLD-D");
        setId(existing, 3L);
        setCreatedAt(existing, OffsetDateTime.now().minusDays(2));

        when(quizRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(quizRepository.save(any(QuizEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        QuizUpdateCommand cmd = new QuizUpdateCommand("NEW", "NEW-D", true, true, 15, true);

        QuizInfo out = quizService.update(3L, cmd);

        assertThat(out.title()).isEqualTo("NEW");
    }

    @Test
    void delete_throwsNotFound_whenMissing() {
        when(quizRepository.existsById(123L)).thenReturn(false);

        assertThatThrownBy(() -> quizService.delete(123L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Quiz not found: 123");

        verify(quizRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_deletes_whenExists() {
        when(quizRepository.existsById(9L)).thenReturn(true);

        quizService.delete(9L);

        verify(quizRepository).deleteById(9L);
    }

    private static void setId(QuizEntity q, long id) {
        try {
            Field f = QuizEntity.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(q, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setCreatedAt(QuizEntity q, OffsetDateTime at) {
        try {
            Field f = QuizEntity.class.getDeclaredField("createdAt");
            f.setAccessible(true);
            f.set(q, at);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
