package com.quiz.quizapp.application.play;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.quizapp.domain.dto.QuestionForPlayDto;

import java.util.*;

final class QuestionOptionsMapper {

    private final ObjectMapper objectMapper;

    QuestionOptionsMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    Map<Long, List<String>> optionsByQuestionId(List<QuestionForPlayDto> questions, boolean randomiseAnswers) {
        Map<Long, List<String>> optionsByQuestionId = new HashMap<>();

        for (var q : questions) {
            String optionsJson = q.optionsJson();
            if (optionsJson == null || optionsJson.isBlank()) continue;

            try {
                JsonNode node = objectMapper.readTree(optionsJson);
                if (!node.isArray()) continue;

                List<String> list = new ArrayList<>();
                node.forEach(n -> list.add(n.asText()));

                if (randomiseAnswers && list.size() > 1) {
                    Collections.shuffle(list);
                }

                optionsByQuestionId.put(q.id(), list);
            } catch (Exception ignored) {
                // preserve existing behaviour
            }
        }

        return optionsByQuestionId;
    }
}
