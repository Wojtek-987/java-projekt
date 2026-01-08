package com.quiz.quizapp.domain.repository;

import com.quiz.quizapp.domain.entity.QuestionEntity;
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
class QuestionRepositoryTest extends PostgresContainerBase {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void findByQuiz_Id_returnsOnlyQuestionsForThatQuiz() {
        var quiz1 = quizRepository.save(new QuizEntity("Quiz1", "d"));
        var quiz2 = quizRepository.save(new QuizEntity("Quiz2", "d"));

        questionRepository.save(newQuestion(quiz1, "SINGLE_CHOICE", "P1", "{\"value\":\"A\"}"));
        questionRepository.save(newQuestion(quiz2, "TRUE_FALSE", "P2", "{\"value\":true}"));

        var page = questionRepository.findByQuiz_Id(quiz1.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void findByQuiz_Id_returnsQuestionsInThatQuiz() {
        var quiz = quizRepository.save(new QuizEntity("Quiz", "d"));

        questionRepository.save(newQuestion(quiz, "SINGLE_CHOICE", "P1", "{\"value\":\"A\"}"));
        questionRepository.save(newQuestion(quiz, "TRUE_FALSE", "P2", "{\"value\":true}"));

        var page = questionRepository.findByQuiz_Id(quiz.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(QuestionEntity::getPrompt).containsExactlyInAnyOrder("P1", "P2");
    }

    @Test
    void findByQuiz_IdAndTypeIgnoreCase_filtersByTypeIgnoringCase() {
        var quiz = quizRepository.save(new QuizEntity("Quiz", "d"));

        questionRepository.save(newQuestion(quiz, "single_choice", "P1", "{\"value\":\"A\"}"));
        questionRepository.save(newQuestion(quiz, "TRUE_FALSE", "P2", "{\"value\":true}"));

        var page = questionRepository.findByQuiz_IdAndTypeIgnoreCase(quiz.getId(), "SINGLE_CHOICE", PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void findByQuiz_IdAndTypeIgnoreCase_returnsMatchingRow() {
        var quiz = quizRepository.save(new QuizEntity("Quiz", "d"));

        questionRepository.save(newQuestion(quiz, "single_choice", "P1", "{\"value\":\"A\"}"));
        questionRepository.save(newQuestion(quiz, "TRUE_FALSE", "P2", "{\"value\":true}"));

        var page = questionRepository.findByQuiz_IdAndTypeIgnoreCase(quiz.getId(), "SINGLE_CHOICE", PageRequest.of(0, 10));

        assertThat(page.getContent().getFirst().getPrompt()).isEqualTo("P1");
    }

    private QuestionEntity newQuestion(QuizEntity quiz, String type, String prompt, String answerKey) {
        var q = new QuestionEntity();
        q.setQuiz(quiz);
        q.setType(type);
        q.setPrompt(prompt);
        q.setPoints(1);
        q.setAnswerKey(answerKey);
        return q;
    }
}
