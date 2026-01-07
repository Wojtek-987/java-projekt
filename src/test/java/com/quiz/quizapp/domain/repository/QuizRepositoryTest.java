package com.quiz.quizapp.domain.repository;

import com.quiz.quizapp.domain.entity.QuizEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.*;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class QuizRepositoryTest extends PostgresDataJpaTestBase {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private EntityManager em;

    @Test
    void saveUpdateAndReload_updatesPersistedState() {
        QuizEntity q = new QuizEntity("Original", "Desc");
        QuizEntity saved = quizRepository.saveAndFlush(q);

        saved.setTitle("Updated Title");
        quizRepository.saveAndFlush(saved);

        QuizEntity reloaded = quizRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void findByTitleContainingIgnoreCase_paginatesAndFilters() {
        quizRepository.save(new QuizEntity("Java Basics", "A"));
        quizRepository.save(new QuizEntity("java advanced", "B"));
        quizRepository.save(new QuizEntity("Spring Boot", "C"));
        quizRepository.flush();

        Page<QuizEntity> page0 = quizRepository.findByTitleContainingIgnoreCase(
                "JAVA",
                PageRequest.of(0, 1, Sort.by("id").ascending())
        );

        assertThat(page0.getTotalElements()).isEqualTo(2);
        assertThat(page0.getContent()).hasSize(1);

        Page<QuizEntity> page1 = quizRepository.findByTitleContainingIgnoreCase(
                "java",
                PageRequest.of(1, 1, Sort.by("id").ascending())
        );

        assertThat(page1.getTotalElements()).isEqualTo(2);
        assertThat(page1.getContent()).hasSize(1);
        assertThat(page1.getContent().getFirst().getTitle().toLowerCase()).contains("java");
    }

    @Test
    void findNegativePointsEnabled_customQueryReturnsOnlyNegativeEnabled() {
        QuizEntity negative = new QuizEntity("Neg", "A");
        negative.setNegativePointsEnabled(true);

        QuizEntity nonNegative = new QuizEntity("NonNeg", "B");
        nonNegative.setNegativePointsEnabled(false);

        quizRepository.save(negative);
        quizRepository.save(nonNegative);
        quizRepository.flush();

        Page<QuizEntity> page = quizRepository.findNegativePointsEnabled(PageRequest.of(0, 10));

        assertThat(page.getContent())
                .extracting(QuizEntity::isNegativePointsEnabled)
                .containsOnly(true);
    }

    @Test
    void findNegativePointsEnabled_ordersByCreatedAtDesc() {
        QuizEntity q1 = new QuizEntity("Neg Old", "A");
        q1.setNegativePointsEnabled(true);
        QuizEntity q2 = new QuizEntity("Neg New", "B");
        q2.setNegativePointsEnabled(true);

        q1 = quizRepository.saveAndFlush(q1);
        q2 = quizRepository.saveAndFlush(q2);

        // No setter for createdAt; force deterministic timestamps via native update.
        OffsetDateTime oldTs = OffsetDateTime.parse("2020-01-01T10:00:00+00:00");
        OffsetDateTime newTs = OffsetDateTime.parse("2021-01-01T10:00:00+00:00");

        em.createNativeQuery("update quizzes set created_at = ? where id = ?")
                .setParameter(1, oldTs)
                .setParameter(2, q1.getId())
                .executeUpdate();

        em.createNativeQuery("update quizzes set created_at = ? where id = ?")
                .setParameter(1, newTs)
                .setParameter(2, q2.getId())
                .executeUpdate();

        em.clear();

        Page<QuizEntity> page = quizRepository.findNegativePointsEnabled(PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(2);

        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Neg New");
        assertThat(page.getContent().get(1).getTitle()).isEqualTo("Neg Old");
    }
}
