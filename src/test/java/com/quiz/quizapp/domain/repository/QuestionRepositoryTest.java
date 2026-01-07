package com.quiz.quizapp.domain.repository;

import com.quiz.quizapp.domain.entity.QuestionEntity;
import com.quiz.quizapp.domain.entity.QuizEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class QuestionRepositoryTest extends PostgresDataJpaTestBase {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void findByQuiz_Id_returnsOnlyQuestionsForThatQuiz_withPagination() {
        QuizEntity quiz1 = quizRepository.saveAndFlush(new QuizEntity("Quiz 1", "A"));
        QuizEntity quiz2 = quizRepository.saveAndFlush(new QuizEntity("Quiz 2", "B"));

        QuestionEntity q1 = new QuestionEntity("SINGLE_CHOICE", "P1", 1);
        q1.setAnswerKey("{\"value\":\"A\"}");
        q1.setQuiz(quiz1);

        QuestionEntity q2 = new QuestionEntity("SINGLE_CHOICE", "P2", 1);
        q2.setAnswerKey("{\"value\":\"B\"}");
        q2.setQuiz(quiz1);

        QuestionEntity qOther = new QuestionEntity("TRUE_FALSE", "Other", 1);
        qOther.setAnswerKey("{\"value\":true}");
        qOther.setQuiz(quiz2);

        questionRepository.save(q1);
        questionRepository.save(q2);
        questionRepository.save(qOther);
        questionRepository.flush();

        Page<QuestionEntity> page = questionRepository.findByQuiz_Id(
                quiz1.getId(),
                PageRequest.of(0, 10, Sort.by("id").ascending())
        );

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(QuestionEntity::getPrompt).containsExactly("P1", "P2");
    }

    @Test
    void findByQuiz_IdAndTypeIgnoreCase_filtersByTypeIgnoringCase() {
        QuizEntity quiz = quizRepository.saveAndFlush(new QuizEntity("Quiz", "A"));

        QuestionEntity single = new QuestionEntity("single_choice", "P1", 1);
        single.setAnswerKey("{\"value\":\"A\"}");
        single.setQuiz(quiz);

        QuestionEntity tf = new QuestionEntity("TRUE_FALSE", "P2", 1);
        tf.setAnswerKey("{\"value\":true}");
        tf.setQuiz(quiz);

        questionRepository.save(single);
        questionRepository.save(tf);
        questionRepository.flush();

        Page<QuestionEntity> singles = questionRepository.findByQuiz_IdAndTypeIgnoreCase(
                quiz.getId(),
                "SINGLE_CHOICE",
                PageRequest.of(0, 10)
        );

        assertThat(singles.getTotalElements()).isEqualTo(1);
        assertThat(singles.getContent().getFirst().getPrompt()).isEqualTo("P1");
    }

    @Test
    void save_withoutQuiz_failsBecauseQuizIsNonNull() {
        QuestionEntity q = new QuestionEntity("SINGLE_CHOICE", "No quiz", 1);
        q.setAnswerKey("{\"value\":\"A\"}");

        assertThatThrownBy(() -> questionRepository.saveAndFlush(q))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void update_points_persists() {
        QuizEntity quiz = quizRepository.saveAndFlush(new QuizEntity("Quiz", "A"));

        QuestionEntity q = new QuestionEntity("SHORT_ANSWER", "What?", 2);
        q.setAnswerKey("{\"value\":\"x\"}");
        q.setQuiz(quiz);

        q = questionRepository.saveAndFlush(q);

        q.setPoints(10);
        questionRepository.saveAndFlush(q);

        QuestionEntity reloaded = questionRepository.findById(q.getId()).orElseThrow();
        assertThat(reloaded.getPoints()).isEqualTo(10);
    }
}
