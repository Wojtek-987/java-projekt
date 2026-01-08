package com.quiz.quizapp.domain.service;

import com.quiz.quizapp.api.dto.RankingRowResponse;
import com.quiz.quizapp.domain.jdbc.RankingJdbcDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RankingService {

    private final RankingJdbcDao rankingJdbcDao;

    public RankingService(RankingJdbcDao rankingJdbcDao) {
        this.rankingJdbcDao = rankingJdbcDao;
    }

    @Transactional(readOnly = true)
    public List<RankingRowResponse> topForQuiz(long quizId, int limit) {
        return rankingJdbcDao.topForQuiz(quizId, limit);
    }
}
