package com.XI.xi_oj.service.impl;

import com.XI.xi_oj.model.dto.judge.JudgeResultDTO;
import com.XI.xi_oj.model.entity.QuestionSubmit;
import com.XI.xi_oj.service.AiJudgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiJudgeServiceImpl implements AiJudgeService {


    @Override
    public JudgeResultDTO submitCode(Long questionId, String code, String language, Long userId) {
        return null;
    }
}
