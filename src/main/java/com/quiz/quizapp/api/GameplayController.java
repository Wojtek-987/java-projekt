package com.quiz.quizapp.api;

import com.quiz.quizapp.api.dto.QuestionForPlayResponse;
import com.quiz.quizapp.api.dto.SubmitAnswersRequest;
import com.quiz.quizapp.api.dto.SubmitAnswersResponse;
import com.quiz.quizapp.domain.dto.QuestionForPlayDto;
import com.quiz.quizapp.domain.dto.SubmitAnswerDto;
import com.quiz.quizapp.domain.dto.SubmitAnswersCommand;
import com.quiz.quizapp.domain.dto.SubmitOutcome;
import com.quiz.quizapp.domain.service.GameplayService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class GameplayController {

    private final GameplayService gameplayService;

    public GameplayController(GameplayService gameplayService) {
        this.gameplayService = gameplayService;
    }

    @GetMapping("/attempts/{attemptId}/questions")
    public ResponseEntity<List<QuestionForPlayResponse>> questions(@PathVariable long attemptId) {
        List<QuestionForPlayDto> qs = gameplayService.questionsForAttempt(attemptId);
        return ResponseEntity.ok(qs.stream().map(this::toResponse).toList());
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<SubmitAnswersResponse> submit(
            @PathVariable long attemptId,
            @Valid @RequestBody SubmitAnswersRequest request
    ) {
        SubmitAnswersCommand cmd = new SubmitAnswersCommand(
                request.answers().stream()
                        .map(a -> new SubmitAnswerDto(a.questionId(), a.answerJson()))
                        .toList()
        );

        SubmitOutcome out = gameplayService.submitAndFinish(attemptId, cmd);
        return ResponseEntity.ok(new SubmitAnswersResponse(out.attemptId(), out.totalScore()));
    }

    private QuestionForPlayResponse toResponse(QuestionForPlayDto q) {
        return new QuestionForPlayResponse(q.id(), q.type(), q.prompt(), q.points(), q.optionsJson());
    }
}
