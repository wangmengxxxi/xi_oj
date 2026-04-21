package com.XI.xi_oj.model.dto.question;

import com.XI.xi_oj.model.entity.AiWrongQuestion;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * 错题封装类（只读 VO，供 OJTools 和 Controller 层返回）
 * 字段名与 OJTools.queryUserWrongQuestion() 调用点严格对齐：
 *   getWrongCode() / getWrongJudgeResult() / getWrongAnalysis() / getReviewCount()
 */
@Data
public class WrongQuestionVO implements Serializable {
    private Long id;
    private Long userId;
    private Long questionId;
    /** 错误代码 */
    private String wrongCode;
    /** 错误判题结果 */
    private String wrongJudgeResult;
    /** AI 错误分析 */
    private String wrongAnalysis;
    /** 复习次数 */
    private Integer reviewCount;
    /** 是否已复习 */
    private Integer isReviewed;
    /**
     * 实体转 VO
     */
    public static WrongQuestionVO objToVo(AiWrongQuestion wrongQuestion) {
        if (wrongQuestion == null) {
            return null;
        }
        WrongQuestionVO vo = new WrongQuestionVO();
        BeanUtils.copyProperties(wrongQuestion, vo);
        return vo;
    }
    private static final long serialVersionUID = 1L;
}