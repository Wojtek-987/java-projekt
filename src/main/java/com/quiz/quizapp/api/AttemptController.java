package com.quiz.quizapp.api;

import com.quiz.quizapp.api.dto.*;
import com.quiz.quizapp.domain.jdbc.RankingJdbcDao;
import com.quiz.quizapp.domain.service.AttemptService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AttemptController {

    private final AttemptService attemptService;
    private final RankingJdbcDao rankingJdbcDao;

    public AttemptController(AttemptService attemptService, RankingJdbcDao rankingJdbcDao) {
        this.attemptService = attemptService;
        this.rankingJdbcDao = rankingJdbcDao;
    }

    @PostMapping("/quizzes/{quizId}/attempts")
    public ResponseEntity<AttemptResponse> start(
            @PathVariable long quizId,
            @Valid @RequestBody StartAttemptRequest request
    ) {
        return ResponseEntity.ok(attemptService.start(quizId, request));
    }

    @PostMapping("/attempts/{attemptId}/finish")
    public ResponseEntity<AttemptResponse> finish(
            @PathVariable long attemptId,
            @Valid @RequestBody FinishAttemptRequest request
    ) {
        return ResponseEntity.ok(attemptService.finish(attemptId, request));
    }

    @GetMapping("/quizzes/{quizId}/ranking")
    public ResponseEntity<List<RankingRowResponse>> ranking(
            @PathVariable long quizId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(rankingJdbcDao.topForQuiz(quizId, limit));
    }
}
