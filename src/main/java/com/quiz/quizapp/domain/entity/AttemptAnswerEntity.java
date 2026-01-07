package com.quiz.quizapp.domain.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


@Entity
@Table(name = "attempt_answers",
        uniqueConstraints = @UniqueConstraint(name = "uk_attempt_question", columnNames = {"attempt_id", "question_id"}))
public class AttemptAnswerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private AttemptEntity attempt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionEntity question;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String answer;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "awarded_points", nullable = false)
    private int awardedPoints;

    @Column(name = "answered_at", nullable = false)
    private OffsetDateTime answeredAt;

    protected AttemptAnswerEntity() {}

    public AttemptAnswerEntity(AttemptEntity attempt, QuestionEntity question, String answer, boolean correct, int awardedPoints) {
        this.attempt = attempt;
        this.question = question;
        this.answer = answer;
        this.correct = correct;
        this.awardedPoints = awardedPoints;
    }

    @PrePersist
    void prePersist() {
        if (answeredAt == null) answeredAt = OffsetDateTime.now();
    }
}
