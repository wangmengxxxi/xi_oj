package com.XI.xi_oj.service.impl;

import com.XI.xi_oj.mapper.AiWrongQuestionMapper;
import com.XI.xi_oj.model.dto.question.WrongQuestionVO;
import com.XI.xi_oj.model.entity.AiWrongQuestion;
import com.XI.xi_oj.service.WrongQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class WrongQuestionServiceImpl
        extends ServiceImpl<AiWrongQuestionMapper, AiWrongQuestion>
        implements WrongQuestionService {
    @Resource
    private AiWrongQuestionMapper aiWrongQuestionMapper;
    @Override
    public WrongQuestionVO getByUserAndQuestion(Long userId, Long questionId) {
        AiWrongQuestion wrongQuestion =
                aiWrongQuestionMapper.selectByUserAndQuestion(userId, questionId);
        return WrongQuestionVO.objToVo(wrongQuestion);
    }
}