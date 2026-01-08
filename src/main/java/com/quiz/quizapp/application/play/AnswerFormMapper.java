package com.quiz.quizapp.application.play;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.quizapp.domain.dto.QuestionForPlayDto;
import org.springframework.util.MultiValueMap;

import java.util.*;

final class AnswerFormMapper {

    private final ObjectMapper objectMapper;

    AnswerFormMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    String buildAnswerJsonFromForm(QuestionForPlayDto q, MultiValueMap<String, String> params) {
        String type = q.type();

        try {
            return switch (type) {
                case "SINGLE_CHOICE", "LIST_CHOICE" -> {
                    String v = params.getFirst("q_" + q.id());
                    if (v == null || v.isBlank()) yield null;
                    yield objectMapper.writeValueAsString(Map.of("value", v));
                }
                case "TRUE_FALSE" -> {
                    String v = params.getFirst("q_" + q.id());
                    if (v == null || v.isBlank()) yield null;
                    boolean b = Boolean.parseBoolean(v);
                    yield objectMapper.writeValueAsString(Map.of("value", b));
                }
                case "MULTI_CHOICE" -> {
                    List<String> vs = params.get("q_" + q.id());
                    if (vs == null || vs.isEmpty()) yield null;
                    var cleaned = vs.stream().filter(s -> s != null && !s.isBlank()).toList();
                    if (cleaned.isEmpty()) yield null;
                    yield objectMapper.writeValueAsString(Map.of("values", cleaned));
                }
                case "SHORT_ANSWER" -> {
                    String v = params.getFirst("q_" + q.id());
                    if (v == null || v.isBlank()) yield null;
                    yield objectMapper.writeValueAsString(Map.of("value", v));
                }
                case "FILL_BLANKS" -> {
                    var values = new ArrayList<String>();
                    for (int i = 0; ; i++) {
                        String v = params.getFirst("q_" + q.id() + "_" + i);
                        if (v == null) break;
                        if (v.isBlank()) yield null;
                        values.add(v);
                    }
                    if (values.isEmpty()) yield null;
                    yield objectMapper.writeValueAsString(Map.of("values", values));
                }
                case "SORTING" -> {
                    String csv = params.getFirst("q_" + q.id() + "_csv");
                    if (csv == null || csv.isBlank()) yield null;
                    var values = Arrays.stream(csv.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .toList();
                    if (values.isEmpty()) yield null;
                    yield objectMapper.writeValueAsString(Map.of("values", values));
                }
                case "MATCHING" -> {
                    String raw = params.getFirst("q_" + q.id() + "_pairs");
                    if (raw == null || raw.isBlank()) yield null;

                    Map<String, String> pairs = new LinkedHashMap<>();
                    for (String line : raw.split("\\R")) {
                        String trimmed = line.trim();
                        if (trimmed.isBlank()) continue;

                        int idx = trimmed.indexOf('=');
                        if (idx <= 0 || idx == trimmed.length() - 1) yield null;

                        String left = trimmed.substring(0, idx).trim();
                        String right = trimmed.substring(idx + 1).trim();
                        if (left.isBlank() || right.isBlank()) yield null;

                        pairs.put(left, right);
                    }
                    if (pairs.isEmpty()) yield null;

                    yield objectMapper.writeValueAsString(Map.of("pairs", pairs));
                }
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }
}
