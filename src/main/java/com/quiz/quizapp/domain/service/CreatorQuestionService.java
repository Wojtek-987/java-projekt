package com.quiz.quizapp.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.quizapp.common.ResourceNotFoundException;
import com.quiz.quizapp.domain.entity.QuestionEntity;
import com.quiz.quizapp.domain.repository.QuestionRepository;
import com.quiz.quizapp.domain.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreatorQuestionService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    public CreatorQuestionService(QuizRepository quizRepository, QuestionRepository questionRepository, ObjectMapper objectMapper) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void create(long quizId, String type, String prompt, int points, String optionsJson, String answerKeyJson) {
        var quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + quizId));

        // Validate JSON (fail fast with IllegalArgumentException)
        validateJsonOrEmpty(optionsJson);
        validateJson(answerKeyJson);

        var q = new QuestionEntity();
        q.setQuiz(quiz);
        q.setType(type.trim().toUpperCase());
        q.setPrompt(prompt.trim());
        q.setPoints(points);
        q.setOptions(blankToNull(optionsJson));
        q.setAnswerKey(answerKeyJson.trim());

        questionRepository.save(q);
    }

    private void validateJsonOrEmpty(String json) {
        if (json == null || json.isBlank()) return;
        validateJson(json);
    }

    private void validateJson(String json) {
        try {
            objectMapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON");
        }
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
