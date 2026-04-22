package com.XI.xi_oj.service;

import com.XI.xi_oj.model.dto.judge.AiCodeAnalysisRequest;
import com.XI.xi_oj.model.entity.AiCodeAnalysis;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiCodeAnalysisService {

    String analyzeCode(Long userId, AiCodeAnalysisRequest request);

    Flux<String> analyzeCodeStream(Long userId, Long questionId, Long questionSubmitId);

    List<AiCodeAnalysis> listMyHistory(Long userId, Long questionId, Integer pageSize);
}
