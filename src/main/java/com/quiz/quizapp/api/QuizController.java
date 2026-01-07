package com.quiz.quizapp.api;

import com.quiz.quizapp.api.dto.CreateQuizRequest;
import com.quiz.quizapp.api.dto.QuizResponse;
import com.quiz.quizapp.domain.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import com.quiz.quizapp.api.dto.UpdateQuizRequest;


@RestController
@RequestMapping("/api/v1/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public ResponseEntity<Page<QuizResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(quizService.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResponse> get(@PathVariable long id) {
        return ResponseEntity.ok(quizService.get(id));
    }

    @PostMapping
    public ResponseEntity<QuizResponse> create(
            @Valid @RequestBody CreateQuizRequest request,
            UriComponentsBuilder ucb
    ) {
        QuizResponse created = quizService.create(request);
        var location = ucb.path("/api/v1/quizzes/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizResponse> update(
            @PathVariable long id,
            @Valid @RequestBody UpdateQuizRequest request
    ) {
        return ResponseEntity.ok(quizService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        quizService.delete(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
