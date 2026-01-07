package com.quiz.quizapp.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateQuizForm {

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 1000)
    private String description;

    private boolean randomiseQuestions;
    private boolean randomiseAnswers;
    private Integer timeLimitSeconds;
    private boolean negativePointsEnabled;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isRandomiseQuestions() { return randomiseQuestions; }
    public void setRandomiseQuestions(boolean randomiseQuestions) { this.randomiseQuestions = randomiseQuestions; }

    public boolean isRandomiseAnswers() { return randomiseAnswers; }
    public void setRandomiseAnswers(boolean randomiseAnswers) { this.randomiseAnswers = randomiseAnswers; }

    public Integer getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(Integer timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }

    public boolean isNegativePointsEnabled() { return negativePointsEnabled; }
    public void setNegativePointsEnabled(boolean negativePointsEnabled) { this.negativePointsEnabled = negativePointsEnabled; }
}
