package com.quiz.quizapp.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.quizapp.domain.entity.QuestionEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScoringServiceTest {

    @Test
    void singleChoice_correctWhenValueMatches() {
        ScoringService scoring = new ScoringService(new ObjectMapper());

        QuestionEntity q = new QuestionEntity("SINGLE_CHOICE", "Pick one", 1);
        q.setAnswerKey("{\"value\":\"A\"}");

        assertThat(scoring.isCorrect(q, "{\"value\":\"A\"}")).isTrue();
        assertThat(scoring.isCorrect(q, "{\"value\":\"B\"}")).isFalse();
    }

    @Test
    void multiChoice_orderDoesNotMatter() {
        ScoringService scoring = new ScoringService(new ObjectMapper());

        QuestionEntity q = new QuestionEntity("MULTI_CHOICE", "Pick many", 1);
        q.setAnswerKey("{\"values\":[\"A\",\"C\"]}");

        assertThat(scoring.isCorrect(q, "{\"values\":[\"C\",\"A\"]}")).isTrue();
        assertThat(scoring.isCorrect(q, "{\"values\":[\"A\"]}")).isFalse();
    }

    @Test
    void shortAnswer_isNormalised_trimCaseAndWhitespace() {
        ScoringService scoring = new ScoringService(new ObjectMapper());

        QuestionEntity q = new QuestionEntity("SHORT_ANSWER", "Type it", 1);
        q.setAnswerKey("{\"value\":\"Hello   World\"}");

        assertThat(scoring.isCorrect(q, "{\"value\":\"  hello world  \"}")).isTrue();
        assertThat(scoring.isCorrect(q, "{\"value\":\"hello  worlds\"}")).isFalse();
    }

    @Test
    void sorting_isOrderSensitive() {
        ScoringService scoring = new ScoringService(new ObjectMapper());

        QuestionEntity q = new QuestionEntity("SORTING", "Order it", 1);
        q.setAnswerKey("{\"values\":[\"1\",\"2\",\"3\"]}");

        assertThat(scoring.isCorrect(q, "{\"values\":[\"1\",\"2\",\"3\"]}")).isTrue();
        assertThat(scoring.isCorrect(q, "{\"values\":[\"3\",\"2\",\"1\"]}")).isFalse();
    }

    @Test
    void matching_requiresExactKeyValuePairs() {
        ScoringService scoring = new ScoringService(new ObjectMapper());

        QuestionEntity q = new QuestionEntity("MATCHING", "Match it", 1);
        q.setAnswerKey("{\"pairs\":{\"a\":\"1\",\"b\":\"2\"}}");

        assertThat(scoring.isCorrect(q, "{\"pairs\":{\"a\":\"1\",\"b\":\"2\"}}")).isTrue();
        assertThat(scoring.isCorrect(q, "{\"pairs\":{\"a\":\"1\",\"b\":\"999\"}}")).isFalse();
    }

    @Test
    void malformedJsonAnswer_isIncorrectNotExplosive() {
        ScoringService scoring = new ScoringService(new ObjectMapper());

        QuestionEntity q = new QuestionEntity("SINGLE_CHOICE", "Pick", 1);
        q.setAnswerKey("{\"value\":\"A\"}");

        assertThat(scoring.isCorrect(q, "{not-json")).isFalse();
    }

    @Test
    void whenObjectMapperThrows_serviceTreatsAsIncorrect() throws Exception {
        ObjectMapper mapper = mock(ObjectMapper.class);
        ScoringService scoring = new ScoringService(mapper);

        QuestionEntity q = new QuestionEntity("SINGLE_CHOICE", "Pick", 1);
        q.setAnswerKey("{\"value\":\"A\"}");

        when(mapper.readTree(anyString())).thenThrow(new RuntimeException("boom"));

        assertThat(scoring.isCorrect(q, "{\"value\":\"A\"}")).isFalse();

        // Prove we actually executed the mapper path:
        verify(mapper, atLeastOnce()).readTree(anyString());
    }
}
