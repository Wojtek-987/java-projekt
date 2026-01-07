package com.quiz.quizapp.domain.repository;

import com.quiz.quizapp.domain.entity.QuizEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuizRepository extends JpaRepository<QuizEntity, Long> {

    // Derived query + pagination
    Page<QuizEntity> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Explicit JPQL query (custom)
    @Query("""
        select q
        from QuizEntity q
        where q.negativePointsEnabled = true
        order by q.createdAt desc
    """)
    Page<QuizEntity> findNegativePointsEnabled(Pageable pageable);
}
