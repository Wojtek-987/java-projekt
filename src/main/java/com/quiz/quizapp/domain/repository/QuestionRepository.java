package com.quiz.quizapp.domain.repository;

import com.quiz.quizapp.domain.entity.QuestionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

    Page<QuestionEntity> findByQuiz_Id(Long quizId, Pageable pageable);

    Page<QuestionEntity> findByQuiz_IdAndTypeIgnoreCase(Long quizId, String type, Pageable pageable);
}
