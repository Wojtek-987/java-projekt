package com.quiz.quizapp.domain.repository;

import com.quiz.quizapp.domain.entity.QuestionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

    // Derived query + pagination (navigates relation quiz.id)
    Page<QuestionEntity> findByQuiz_Id(Long quizId, Pageable pageable);

    // Derived query (filter by type within a quiz)
    Page<QuestionEntity> findByQuiz_IdAndTypeIgnoreCase(Long quizId, String type, Pageable pageable);
}
