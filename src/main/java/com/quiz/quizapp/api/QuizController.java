package com.quiz.quizapp.api;

import com.quiz.quizapp.api.dto.CreateQuizRequest;
import com.quiz.quizapp.api.dto.QuizResponse;
import com.quiz.quizapp.api.dto.UpdateQuizRequest;
import com.quiz.quizapp.domain.dto.QuizCreateCommand;
import com.quiz.quizapp.domain.dto.QuizInfo;
import com.quiz.quizapp.domain.dto.QuizUpdateCommand;
import com.quiz.quizapp.domain.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public ResponseEntity<Page<QuizResponse>> list(Pageable pageable) {
        Page<QuizInfo> page = quizService.list(pageable);
        return ResponseEntity.ok(page.map(this::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResponse> get(@PathVariable long id) {
        return ResponseEntity.ok(toResponse(quizService.get(id)));
    }

    @PostMapping
    public ResponseEntity<QuizResponse> create(
            @Valid @RequestBody CreateQuizRequest request,
            UriComponentsBuilder ucb
    ) {
        QuizInfo created = quizService.create(new QuizCreateCommand(
                request.title(),
                request.description(),
                request.randomiseQuestions(),
                request.randomiseAnswers(),
                request.timeLimitSeconds(),
                request.negativePointsEnabled()
        ));
        var location = ucb.path("/api/v1/quizzes/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizResponse> update(
            @PathVariable long id,
            @Valid @RequestBody UpdateQuizRequest request
    ) {
        QuizInfo updated = quizService.update(id, new QuizUpdateCommand(
                request.title(),
                request.description(),
                request.randomiseQuestions(),
                request.randomiseAnswers(),
                request.timeLimitSeconds(),
                request.negativePointsEnabled()
        ));
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        quizService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private QuizResponse toResponse(QuizInfo q) {
        return new QuizResponse(
                q.id(),
                q.title(),
                q.description(),
                q.randomiseQuestions(),
                q.randomiseAnswers(),
                q.timeLimitSeconds(),
                q.negativePointsEnabled(),
                q.createdAt()
        );
    }
}
