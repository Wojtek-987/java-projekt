package com.quiz.quizapp.api;

import com.quiz.quizapp.common.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestThrowingController {

    @GetMapping("/notfound")
    public String notFound() {
        throw new ResourceNotFoundException("Missing");
    }

    @GetMapping("/conflict")
    public String conflict() {
        throw new IllegalStateException("Nope");
    }

    @PostMapping("/validate")
    public String validate(@Valid @RequestBody NameDto dto) {
        return "ok";
    }

    public record NameDto(@NotBlank String name) {}
}
