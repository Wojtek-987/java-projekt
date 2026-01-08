package com.quiz.quizapp.domain.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "quizzes")
public class QuizEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "randomise_questions", nullable = false)
    private boolean randomiseQuestions;

    @Column(name = "randomise_answers", nullable = false)
    private boolean randomiseAnswers;

    @Column(name = "time_limit_seconds")
    private Integer timeLimitSeconds;

    @Column(name = "negative_points_enabled", nullable = false)
    private boolean negativePointsEnabled;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;


    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionEntity> questions = new ArrayList<>();

    protected QuizEntity() {}

    public QuizEntity(String title, String description) {
        this.title = title;
        this.description = description;
    }

    @ManyToMany
    @JoinTable(
            name = "quiz_tags",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagEntity> tags = new HashSet<>();


    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    // --- getters/setters ---

    public Long getId() { return id; }

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

    public OffsetDateTime getCreatedAt() { return createdAt; }

    public List<QuestionEntity> getQuestions() { return questions; }

    public void addQuestion(QuestionEntity q) {
        questions.add(q);
        q.setQuiz(this);
    }

    public void removeQuestion(QuestionEntity q) {
        questions.remove(q);
        q.setQuiz(null);
    }
}
