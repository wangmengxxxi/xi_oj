package com.XI.xi_oj.model.dto.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 错题上下文类，用于封装错题相关信息
 * 使用了Lombok注解简化代码
 * @Data: 自动生成getter、setter、toString等方法
 * @Builder: 提供建造者模式支持
 * @NoArgsConstructor: 提供无参构造方法
 * @AllArgsConstructor: 提供全参构造方法
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WrongQuestionContext {

    // 错题ID，唯一标识一道错题
    private Long wrongQuestionId;

    // 题目ID，关联到原始题目
    private Long questionId;

    // 题目标题
    private String title;

    // 题目内容
    private String content;

    // 题目标签，用于分类和检索
    private String tags;

    // 题目难度等级
    private String difficulty;

    // 错误的代码实现
    private String wrongCode;

    // 编程语言类型
    private String language;

    private String wrongJudgeResult;

    private String errorMsg;

    private Long userId;
}
