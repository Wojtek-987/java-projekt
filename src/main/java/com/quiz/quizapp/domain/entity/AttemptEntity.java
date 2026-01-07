package com.quiz.quizapp.domain.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "attempts")
public class AttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private QuizEntity quiz;

    @Column(nullable = false, length = 60)
    private String nickname;

    @Column(nullable = false)
    private int score;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    protected AttemptEntity() {}

    public AttemptEntity(QuizEntity quiz, String nickname) {
        this.quiz = quiz;
        this.nickname = nickname;
        this.score = 0;
    }

    @PrePersist
    void prePersist() {
        if (startedAt == null) startedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public QuizEntity getQuiz() { return quiz; }
    public String getNickname() { return nickname; }
    public int getScore() { return score; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getFinishedAt() { return finishedAt; }

    public void setScore(int score) { this.score = score; }
    public void finishNow() { this.finishedAt = OffsetDateTime.now(); }
}
