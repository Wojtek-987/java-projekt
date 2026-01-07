package com.quiz.quizapp.domain.repository;

import com.quiz.quizapp.domain.entity.AttemptEntity;
import com.quiz.quizapp.domain.entity.QuizEntity;
import jakarta.persistence.EntityManager;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class AttemptRepositoryTest extends PostgresDataJpaTestBase {

    @Autowired
    private AttemptRepository attemptRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private EntityManager em;

    @Test
    void saveAndFindById_roundTrip() {
        QuizEntity quiz = quizRepository.saveAndFlush(new QuizEntity("Quiz", "A"));
        AttemptEntity attempt = attemptRepository.saveAndFlush(new AttemptEntity(quiz, "Wojtek"));

        AttemptEntity reloaded = attemptRepository.findById(attempt.getId()).orElseThrow();
        assertThat(reloaded.getNickname()).isEqualTo("Wojtek");
        assertThat(reloaded.getScore()).isZero();
    }

    @Test
    void findWithQuizById_entityGraphLoadsQuizEvenAfterClear() {
        QuizEntity quiz = quizRepository.saveAndFlush(new QuizEntity("Graph Quiz", "A"));
        AttemptEntity attempt = attemptRepository.saveAndFlush(new AttemptEntity(quiz, "Player"));

        em.clear();

        AttemptEntity loaded = attemptRepository.findWithQuizById(attempt.getId()).orElseThrow();

        // Should not throw LazyInitializationException due to EntityGraph
        assertThatCode(() -> loaded.getQuiz().getTitle()).doesNotThrowAnyException();
        assertThat(loaded.getQuiz().getTitle()).isEqualTo("Graph Quiz");
    }

    @Test
    void plainFindById_doesNotEagerlyLoadQuiz_byDefault() {
        QuizEntity quiz = quizRepository.saveAndFlush(new QuizEntity("Lazy Quiz", "A"));
        AttemptEntity attempt = attemptRepository.saveAndFlush(new AttemptEntity(quiz, "Player"));

        em.clear();

        AttemptEntity loaded = attemptRepository.findById(attempt.getId()).orElseThrow();

        // Deterministic: verify association is not eagerly loaded by default.
        // We do NOT dereference loaded.getQuiz().getTitle() because that would initialise it.
        var persistenceUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
        assertThat(persistenceUtil.isLoaded(loaded, "quiz")).isFalse();
    }

}
