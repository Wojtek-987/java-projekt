package com.quiz.quizapp.api;

import com.quiz.quizapp.api.dto.QuestionForPlayResponse;
import com.quiz.quizapp.api.dto.SubmitAnswersRequest;
import com.quiz.quizapp.api.dto.SubmitAnswersResponse;
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
        return ResponseEntity.ok(gameplayService.questionsForAttempt(attemptId));
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<SubmitAnswersResponse> submit(
            @PathVariable long attemptId,
            @Valid @RequestBody SubmitAnswersRequest request
    ) {
        return ResponseEntity.ok(gameplayService.submitAndFinish(attemptId, request));
    }
}
