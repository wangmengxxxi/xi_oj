package com.XI.xi_oj.service;

import com.XI.xi_oj.model.dto.question.WrongQuestionVO;
import com.XI.xi_oj.model.entity.AiWrongQuestion;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 针对表【ai_wrong_question(AI错题本)】的数据库操作 Service
 * 供 OJTools 与错题收集器注入
 */
public interface WrongQuestionService extends IService<AiWrongQuestion> {
    /**
     * 查询指定用户的指定题目错题记录，并封装为 VO
     *
     * @param userId     用户ID
     * @param questionId 题目ID
     * @return 错题 VO；无记录时返回 null
     */
    WrongQuestionVO getByUserAndQuestion(Long userId, Long questionId);
}