package com.quiz.quizapp.domain.service;

import com.quiz.quizapp.api.dto.QuestionForPlayResponse;
import com.quiz.quizapp.api.dto.SubmitAnswersRequest;
import com.quiz.quizapp.api.dto.SubmitAnswersResponse;
import com.quiz.quizapp.common.ResourceNotFoundException;
import com.quiz.quizapp.domain.entity.AttemptAnswerEntity;
import com.quiz.quizapp.domain.repository.AttemptAnswerRepository;
import com.quiz.quizapp.domain.repository.AttemptRepository;
import com.quiz.quizapp.domain.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;



import java.util.*;

@Service
public class GameplayService {

    private final AttemptRepository attemptRepository;
    private final QuestionRepository questionRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final ScoringService scoringService;
    private final ObjectMapper objectMapper;


    public GameplayService(
            AttemptRepository attemptRepository,
            QuestionRepository questionRepository,
            AttemptAnswerRepository attemptAnswerRepository,
            ScoringService scoringService,
            ObjectMapper objectMapper
    ) {
        this.attemptRepository = attemptRepository;
        this.questionRepository = questionRepository;
        this.attemptAnswerRepository = attemptAnswerRepository;
        this.scoringService = scoringService;
        this.objectMapper = objectMapper;
    }


    @Transactional(readOnly = true)
    public List<QuestionForPlayResponse> questionsForAttempt(long attemptId) {
        var attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found: " + attemptId));

        var quiz = attempt.getQuiz();
        var page = questionRepository.findByQuiz_Id(quiz.getId(), org.springframework.data.domain.Pageable.unpaged());
        List<QuestionForPlayResponse> questions = page.getContent().stream()
                .map(q -> new QuestionForPlayResponse(q.getId(), q.getType(), q.getPrompt(), q.getPoints(), q.getOptions()))
                .toList();

        if (attempt.getFinishedAt() != null) {
            throw new IllegalStateException("Attempt already finished");
        }


        if (quiz.isRandomiseQuestions()) {
            List<QuestionForPlayResponse> shuffled = new ArrayList<>(questions);
            Collections.shuffle(shuffled);
            return shuffled;
        }
        return questions;
    }

    @Transactional
    public SubmitAnswersResponse submitAndFinish(long attemptId, SubmitAnswersRequest req) {
        var attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found: " + attemptId));

        if (attempt.getFinishedAt() != null) {
            throw new IllegalStateException("Attempt already finished");
        }


        var quiz = attempt.getQuiz();
        boolean negativeEnabled = quiz.isNegativePointsEnabled();

        Integer limitSeconds = quiz.getTimeLimitSeconds();
        if (limitSeconds != null && limitSeconds > 0) {
            OffsetDateTime deadline = attempt.getStartedAt().plusSeconds(limitSeconds.longValue());
            if (OffsetDateTime.now().isAfter(deadline)) {
                throw new IllegalStateException("Time limit exceeded");
            }
        }

        int total = 0;

        for (var ansReq : req.answers()) {
            var q = questionRepository.findById(ansReq.questionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + ansReq.questionId()));

            // Ensure the question belongs to the quiz
            if (!Objects.equals(q.getQuiz().getId(), quiz.getId())) {
                throw new IllegalArgumentException("Question does not belong to quiz");
            }

            boolean correct = scoringService.isCorrect(q, ansReq.answerJson());
            int points = q.getPoints();
            int awarded = correct ? points : (negativeEnabled ? -points : 0);

            attemptAnswerRepository.save(new AttemptAnswerEntity(attempt, q, ansReq.answerJson(), correct, awarded));
            total += awarded;
        }

        attempt.setScore(total);
        attempt.finishNow();

        return new SubmitAnswersResponse(attempt.getId(), attempt.getScore());
    }
}
