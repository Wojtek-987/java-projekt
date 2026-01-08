package com.quiz.quizapp.application.play;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.quizapp.domain.dto.QuestionForPlayDto;
import com.quiz.quizapp.domain.dto.SubmitAnswerDto;
import com.quiz.quizapp.domain.dto.SubmitAnswersCommand;
import com.quiz.quizapp.domain.service.AttemptReadService;
import com.quiz.quizapp.domain.service.AttemptService;
import com.quiz.quizapp.domain.service.GameplayService;
import com.quiz.quizapp.domain.service.QuizReadService;
import com.quiz.quizapp.domain.service.RankingService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.*;

@Component
public class PlayFacade {

    private final QuizReadService quizReadService;
    private final AttemptReadService attemptReadService;
    private final AttemptService attemptService;
    private final GameplayService gameplayService;
    private final RankingService rankingService;
    private final QuestionOptionsMapper questionOptionsMapper;
    private final AnswerFormMapper answerFormMapper;

    public PlayFacade(
            QuizReadService quizReadService,
            AttemptReadService attemptReadService,
            AttemptService attemptService,
            GameplayService gameplayService,
            RankingService rankingService,
            ObjectMapper objectMapper
    ) {
        this.quizReadService = quizReadService;
        this.attemptReadService = attemptReadService;
        this.attemptService = attemptService;
        this.gameplayService = gameplayService;
        this.rankingService = rankingService;
        this.questionOptionsMapper = new QuestionOptionsMapper(objectMapper);
        this.answerFormMapper = new AnswerFormMapper(objectMapper);
    }

    public List<?> listPlayableQuizzes() {
        return quizReadService.list(PageRequest.of(0, 50)).getContent();
    }

    public Map<String, Object> getStartViewModel(long quizId) {
        var quiz = quizReadService.getOrThrow(quizId);
        return Map.of("quiz", quiz);
    }

    public long startAttempt(long quizId, String nickname) {
        return attemptService.start(quizId, nickname).id();
    }

    public Map<String, Object> getAttemptViewModel(long attemptId) {
        var attempt = attemptReadService.getWithQuizOrThrow(attemptId);
        var quiz = attempt.getQuiz();

        var questions = gameplayService.questionsForAttempt(attemptId);
        Map<Long, List<String>> optionsByQuestionId =
                questionOptionsMapper.optionsByQuestionId(questions, quiz.isRandomiseAnswers());

        Map<String, Object> model = new HashMap<>();
        model.put("attempt", attempt);
        model.put("quiz", quiz);
        model.put("questions", questions);
        model.put("optionsByQuestionId", optionsByQuestionId);

        model.put("quizTitle", quiz.getTitle());
        model.put("timeLimitSeconds", quiz.getTimeLimitSeconds());
        model.put("negativePointsEnabled", quiz.isNegativePointsEnabled());

        return model;
    }

    public SubmitOutcome submitAttempt(long attemptId, MultiValueMap<String, String> params) {
        var attempt = attemptReadService.getWithQuizOrThrow(attemptId);
        long quizId = attempt.getQuiz().getId();

        var questions = gameplayService.questionsForAttempt(attemptId);
        List<SubmitAnswerDto> answers = new ArrayList<>(questions.size());

        for (QuestionForPlayDto q : questions) {
            String answerJson = answerFormMapper.buildAnswerJsonFromForm(q, params);
            if (answerJson == null || answerJson.isBlank()) {
                throw new IncompleteAnswersException("You must answer every question.");
            }
            answers.add(new SubmitAnswerDto(q.id(), answerJson));
        }

        var result = gameplayService.submitAndFinish(attemptId, new SubmitAnswersCommand(answers));
        return new SubmitOutcome(quizId, result.totalScore());
    }

    public Map<String, Object> getRankingViewModel(long quizId, int limit) {
        var quiz = quizReadService.getOrThrow(quizId);
        var rows = rankingService.topForQuiz(quizId, limit);

        Map<String, Object> model = new HashMap<>();
        model.put("quiz", quiz);
        model.put("rows", rows);
        return model;
    }

    public record SubmitOutcome(long quizId, int totalScore) {}

    public static final class IncompleteAnswersException extends RuntimeException {
        public IncompleteAnswersException(String message) {
            super(message);
        }
    }
}
