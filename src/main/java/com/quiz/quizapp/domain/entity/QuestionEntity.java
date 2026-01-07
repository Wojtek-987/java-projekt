package com.quiz.quizapp.domain.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


@Entity
@Table(name = "questions")
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // we’ll later formalise this as an enum + polymorphism; for now string keeps migrations simple
    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 2000)
    private String prompt;

    @Column(nullable = false)
    private int points = 1;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String options;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answer_key", nullable = false, columnDefinition = "jsonb")
    private String answerKey = "{}";



    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private QuizEntity quiz;

    public QuestionEntity() {}

    public QuestionEntity(String type, String prompt, int points) {
        this.type = type;
        this.prompt = prompt;
        this.points = points;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    // --- getters/setters ---

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    public String getAnswerKey() { return answerKey; }
    public void setAnswerKey(String answerKey) { this.answerKey = answerKey; }

    public Long getId() { return id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public OffsetDateTime getCreatedAt() { return createdAt; }

    public QuizEntity getQuiz() { return quiz; }
    public void setQuiz(QuizEntity quiz) { this.quiz = quiz; }
}
