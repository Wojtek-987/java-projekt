package com.quiz.quizapp.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class CreateQuestionForm {

    @NotNull
    private Long quizId;

    @NotBlank
    @Size(max = 1000)
    private String prompt;

    @NotBlank
    @Size(max = 50)
    private String type; // matches the strings used in scoring service

    @NotNull
    @PositiveOrZero
    private Integer points;

    // optional JSON
    private String optionsJson;

    @NotBlank
    private String answerKeyJson;

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public String getOptionsJson() { return optionsJson; }
    public void setOptionsJson(String optionsJson) { this.optionsJson = optionsJson; }

    public String getAnswerKeyJson() { return answerKeyJson; }
    public void setAnswerKeyJson(String answerKeyJson) { this.answerKeyJson = answerKeyJson; }
}
