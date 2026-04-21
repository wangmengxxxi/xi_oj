package com.XI.xi_oj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * AI 错题本
 * 对应数据库表：ai_wrong_question
 * 注意：项目配置 map-underscore-to-camel-case: false，
 * 因此下划线列名必须使用 @TableField 显式映射
 */
@TableName(value = "ai_wrong_question")
@Data
public class AiWrongQuestion implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("question_id")
    private Long questionId;
    /** 用户提交的错误代码 */
    @TableField("wrong_code")
    private String wrongCode;
    /** 错误判题结果（非 Accepted 的状态文本，如 Wrong Answer） */
    @TableField("wrong_judge_result")
    private String wrongJudgeResult;
    /** AI 生成的错误原因分析 */
    @TableField("wrong_analysis")
    private String wrongAnalysis;
    /** AI 生成的复习计划 */
    @TableField("review_plan")
    private String reviewPlan;
    /** AI 推荐的同类题目 ID（JSON 数组，如 [12, 34, 56]） */
    @TableField("similar_questions")
    private String similarQuestions;
    /** 是否已复习（0-否 1-是） */
    @TableField("is_reviewed")
    private Integer isReviewed;
    /** 复习次数 */
    @TableField("review_count")
    private Integer reviewCount;
    /** 下次复习时间（艾宾浩斯遗忘曲线调度） */
    @TableField("next_review_time")
    private Date nextReviewTime;
    private Date createTime;
    private Date updateTime;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}