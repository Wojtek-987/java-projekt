package com.quiz.quizapp.domain.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80, unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<QuizEntity> quizzes = new HashSet<>();

    protected TagEntity() {}

    public TagEntity(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Set<QuizEntity> getQuizzes() { return quizzes; }
}
