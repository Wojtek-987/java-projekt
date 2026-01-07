package com.quiz.quizapp.domain.repository;

import com.quiz.quizapp.domain.entity.AttemptAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswerEntity, Long> {}
