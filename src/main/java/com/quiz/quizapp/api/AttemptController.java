package com.quiz.quizapp.api;

import com.quiz.quizapp.api.dto.AttemptResponse;
import com.quiz.quizapp.api.dto.FinishAttemptRequest;
import com.quiz.quizapp.api.dto.RankingRowResponse;
import com.quiz.quizapp.api.dto.StartAttemptRequest;
import com.quiz.quizapp.domain.dto.AttemptInfo;
import com.quiz.quizapp.domain.service.AttemptService;
import com.quiz.quizapp.domain.service.RankingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AttemptController {

    private final AttemptService attemptService;
    private final RankingService rankingService;

    public AttemptController(AttemptService attemptService, RankingService rankingService) {
        this.attemptService = attemptService;
        this.rankingService = rankingService;
    }

    @PostMapping("/quizzes/{quizId}/attempts")
    public ResponseEntity<AttemptResponse> start(
            @PathVariable long quizId,
            @Valid @RequestBody StartAttemptRequest request
    ) {
        AttemptInfo info = attemptService.start(quizId, request.nickname());
        return ResponseEntity.ok(toResponse(info));
    }

    @PostMapping("/attempts/{attemptId}/finish")
    public ResponseEntity<AttemptResponse> finish(
            @PathVariable long attemptId,
            @Valid @RequestBody FinishAttemptRequest request
    ) {
        AttemptInfo info = attemptService.finish(attemptId, request.score());
        return ResponseEntity.ok(toResponse(info));
    }

    @GetMapping("/quizzes/{quizId}/ranking")
    public ResponseEntity<List<RankingRowResponse>> ranking(
            @PathVariable long quizId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(rankingService.topForQuiz(quizId, limit));
    }

    private AttemptResponse toResponse(AttemptInfo a) {
        return new AttemptResponse(
                a.id(),
                a.quizId(),
                a.nickname(),
                a.score(),
                a.startedAt(),
                a.finishedAt()
        );
    }
}
