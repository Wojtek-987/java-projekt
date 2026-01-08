package com.quiz.quizapp.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.quizapp.domain.entity.QuestionEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScoringServiceTest {

    private final ScoringService scoringService = new ScoringService(new ObjectMapper());

    @Test
    void singleChoice_returnsTrueWhenValuesMatch() {
        var q = question("SINGLE_CHOICE", "{\"value\":\"A\"}");
        assertThat(scoringService.isCorrect(q, "{\"value\":\"A\"}")).isTrue();
    }

    @Test
    void singleChoice_returnsFalseWhenValuesDiffer() {
        var q = question("SINGLE_CHOICE", "{\"value\":\"A\"}");
        assertThat(scoringService.isCorrect(q, "{\"value\":\"B\"}")).isFalse();
    }

    @Test
    void multiChoice_returnsTrueWhenSameSetIgnoringOrder() {
        var q = question("MULTI_CHOICE", "{\"values\":[\"A\",\"B\"]}");
        assertThat(scoringService.isCorrect(q, "{\"values\":[\"B\",\"A\"]}")).isTrue();
    }

    @Test
    void multiChoice_returnsFalseWhenSetDiffers() {
        var q = question("MULTI_CHOICE", "{\"values\":[\"A\",\"B\"]}");
        assertThat(scoringService.isCorrect(q, "{\"values\":[\"A\"]}")).isFalse();
    }

    @Test
    void trueFalse_returnsTrueWhenBooleanMatches() {
        var q = question("TRUE_FALSE", "{\"value\":true}");
        assertThat(scoringService.isCorrect(q, "{\"value\":true}")).isTrue();
    }

    @Test
    void trueFalse_returnsFalseWhenBooleanDiffers() {
        var q = question("TRUE_FALSE", "{\"value\":true}");
        assertThat(scoringService.isCorrect(q, "{\"value\":false}")).isFalse();
    }

    @Test
    void shortAnswer_normalisesWhitespaceAndCase() {
        var q = question("SHORT_ANSWER", "{\"value\":\"Hello  World\"}");
        assertThat(scoringService.isCorrect(q, "{\"value\":\"  hello world \"}")).isTrue();
    }

    @Test
    void fillBlanks_normalisesEachEntry() {
        var q = question("FILL_BLANKS", "{\"values\":[\"Foo\",\"Bar\"]}");
        assertThat(scoringService.isCorrect(q, "{\"values\":[\" foo \",\"bar\"]}")).isTrue();
    }

    @Test
    void sorting_returnsTrueWhenExactOrderMatches() {
        var q = question("SORTING", "{\"values\":[\"1\",\"2\",\"3\"]}");
        assertThat(scoringService.isCorrect(q, "{\"values\":[\"1\",\"2\",\"3\"]}")).isTrue();
    }

    @Test
    void sorting_returnsFalseWhenOrderDiffers() {
        var q = question("SORTING", "{\"values\":[\"1\",\"2\",\"3\"]}");
        assertThat(scoringService.isCorrect(q, "{\"values\":[\"3\",\"2\",\"1\"]}")).isFalse();
    }

    @Test
    void matching_returnsTrueWhenExactMapMatches() {
        var q = question("MATCHING", "{\"pairs\":{\"A\":\"1\",\"B\":\"2\"}}");
        assertThat(scoringService.isCorrect(q, "{\"pairs\":{\"A\":\"1\",\"B\":\"2\"}}")).isTrue();
    }

    @Test
    void matching_returnsFalseWhenMapDiffers() {
        var q = question("MATCHING", "{\"pairs\":{\"A\":\"1\",\"B\":\"2\"}}");
        assertThat(scoringService.isCorrect(q, "{\"pairs\":{\"A\":\"2\",\"B\":\"2\"}}")).isFalse();
    }

    @Test
    void malformedJson_isTreatedAsIncorrect() {
        var q = question("SINGLE_CHOICE", "{\"value\":\"A\"}");
        assertThat(scoringService.isCorrect(q, "not-json")).isFalse();
    }

    private static QuestionEntity question(String type, String answerKey) {
        var q = new QuestionEntity();
        q.setType(type);
        q.setAnswerKey(answerKey);
        q.setPrompt("p");
        q.setPoints(1);
        return q;
    }
}
