package com.XI.xi_oj.service;

import com.XI.xi_oj.model.dto.question.AiQuestionParseResponse;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiQuestionParseService {

    AiQuestionParseResponse parseQuestion(Long userId, Long questionId);

    Flux<String> parseQuestionStream(Long userId, Long questionId);

    List<Long> listSimilarQuestionIds(Long userId, Long questionId);
}
