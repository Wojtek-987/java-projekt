package com.quiz.quizapp.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.quizapp.domain.entity.QuestionEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ScoringService {

    private final ObjectMapper objectMapper;

    public ScoringService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public boolean isCorrect(QuestionEntity q, String answerJson) {
        try {
            JsonNode key = objectMapper.readTree(q.getAnswerKey());
            JsonNode ans = objectMapper.readTree(answerJson);

            String type = q.getType().toUpperCase(Locale.ROOT);

            return switch (type) {
                case "SINGLE_CHOICE" -> eqText(ans.get("value"), key.get("value"));
                case "MULTI_CHOICE" -> eqStringSet(ans.get("values"), key.get("values"));
                case "TRUE_FALSE" -> ans.get("value").asBoolean() == key.get("value").asBoolean();
                case "SHORT_ANSWER" -> eqNormalised(ans.get("value"), key.get("value"));
                case "LIST_CHOICE" -> eqText(ans.get("value"), key.get("value"));
                case "FILL_BLANKS" -> eqNormalisedList(ans.get("values"), key.get("values"));
                case "SORTING" -> eqStringList(ans.get("values"), key.get("values"));
                case "MATCHING" -> eqStringMap(ans.get("pairs"), key.get("pairs"));
                default -> false;
            };
        } catch (Exception e) {
            // Malformed JSON => treat as incorrect
            return false;
        }
    }

    private boolean eqText(JsonNode a, JsonNode b) {
        if (a == null || b == null || a.isNull() || b.isNull()) return false;
        return Objects.equals(a.asText(), b.asText());
    }

    private boolean eqNormalised(JsonNode a, JsonNode b) {
        if (a == null || b == null || a.isNull() || b.isNull()) return false;
        return normalise(a.asText()).equals(normalise(b.asText()));
    }

    private boolean eqStringSet(JsonNode a, JsonNode b) {
        return toSet(a).equals(toSet(b));
    }

    private boolean eqStringList(JsonNode a, JsonNode b) {
        return toList(a).equals(toList(b));
    }

    private boolean eqNormalisedList(JsonNode a, JsonNode b) {
        List<String> la = toList(a).stream().map(this::normalise).toList();
        List<String> lb = toList(b).stream().map(this::normalise).toList();
        return la.equals(lb);
    }

    private boolean eqStringMap(JsonNode a, JsonNode b) {
        return toMap(a).equals(toMap(b));
    }

    private Set<String> toSet(JsonNode node) {
        if (node == null || !node.isArray()) return Set.of();
        Set<String> s = new HashSet<>();
        node.forEach(n -> s.add(n.asText()));
        return s;
    }

    private List<String> toList(JsonNode node) {
        if (node == null || !node.isArray()) return List.of();
        List<String> l = new ArrayList<>();
        node.forEach(n -> l.add(n.asText()));
        return l;
    }

    private Map<String, String> toMap(JsonNode node) {
        if (node == null || !node.isObject()) return Map.of();
        Map<String, String> m = new HashMap<>();
        node.fields().forEachRemaining(e -> m.put(e.getKey(), e.getValue().asText()));
        return m;
    }

    private String normalise(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }
}
