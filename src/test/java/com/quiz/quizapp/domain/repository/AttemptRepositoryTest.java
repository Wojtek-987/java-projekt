package com.quiz.quizapp.domain.repository;

import com.quiz.quizapp.domain.entity.AttemptEntity;
import com.quiz.quizapp.domain.entity.QuizEntity;
import com.quiz.quizapp.testsupport.PostgresContainerBase;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AttemptRepositoryTest extends PostgresContainerBase {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private AttemptRepository attemptRepository;

    @Test
    void findWithQuizById_entityGraphInitialisesQuiz() {
        var quiz = quizRepository.save(new QuizEntity("Quiz", "d"));
        var attempt = attemptRepository.save(new AttemptEntity(quiz, "nick"));

        var loaded = attemptRepository.findWithQuizById(attempt.getId()).orElseThrow();

        assertThat(loaded.getQuiz()).isNotNull();
        assertThat(Hibernate.isInitialized(loaded.getQuiz())).isTrue();
        assertThat(loaded.getQuiz().getTitle()).isEqualTo("Quiz");
    }
}
