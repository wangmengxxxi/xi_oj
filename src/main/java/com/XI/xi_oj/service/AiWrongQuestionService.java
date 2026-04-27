package com.XI.xi_oj.service;

import com.XI.xi_oj.model.dto.question.WrongQuestionVO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiWrongQuestionService {

    List<WrongQuestionVO> listMyWrongQuestions(Long userId);

    List<WrongQuestionVO> listDueReviewQuestions(Long userId);

    String analyzeWrongQuestion(Long userId, Long wrongQuestionId);

    Flux<String> analyzeWrongQuestionStream(Long userId, Long wrongQuestionId);

    void markReviewed(Long userId, Long wrongQuestionId);
}
