package com.quiz.quizapp.domain.repository;

import com.quiz.quizapp.domain.entity.QuizEntity;
import com.quiz.quizapp.testsupport.PostgresContainerBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class QuizRepositoryTest extends PostgresContainerBase {

    @Autowired
    private QuizRepository quizRepository;

    @Test
    void findByTitleContainingIgnoreCase_returnsMatches() {
        quizRepository.save(new QuizEntity("Java Basics", "desc"));
        quizRepository.save(new QuizEntity("Spring Boot", "desc"));
        quizRepository.save(new QuizEntity("Kotlin", "desc"));

        var page = quizRepository.findByTitleContainingIgnoreCase("java", PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(QuizEntity::getTitle).containsExactly("Java Basics");
    }

    @Test
    void findNegativePointsEnabled_returnsOnlyEnabledQuizzes() {
        var enabled = new QuizEntity("A", "d");
        enabled.setNegativePointsEnabled(true);
        quizRepository.save(enabled);

        var disabled = new QuizEntity("B", "d");
        disabled.setNegativePointsEnabled(false);
        quizRepository.save(disabled);

        var page = quizRepository.findNegativePointsEnabled(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void findNegativePointsEnabled_returnsEnabledQuizMetadata() {
        var enabled = new QuizEntity("A", "d");
        enabled.setNegativePointsEnabled(true);
        quizRepository.save(enabled);

        var disabled = new QuizEntity("B", "d");
        disabled.setNegativePointsEnabled(false);
        quizRepository.save(disabled);

        var page = quizRepository.findNegativePointsEnabled(PageRequest.of(0, 10));

        var first = page.getContent().getFirst();
        assertThat(first.isNegativePointsEnabled()).isTrue();
        assertThat(first.getTitle()).isEqualTo("A");
    }
}
