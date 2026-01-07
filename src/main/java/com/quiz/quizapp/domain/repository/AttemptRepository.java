package com.quiz.quizapp.domain.repository;

import java.util.Optional;

import com.quiz.quizapp.domain.entity.AttemptEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttemptRepository extends JpaRepository<AttemptEntity, Long> {

    @EntityGraph(attributePaths = "quiz")
    Optional<AttemptEntity> findWithQuizById(Long id);
}
