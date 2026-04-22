package com.XI.xi_oj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("ai_code_analysis")
public class AiCodeAnalysis implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("question_id")
    private Long questionId;

    private String code;

    private String language;

    @TableField("analysis_result")
    private String analysisResult;

    private Integer score;

    @TableField("judge_result")
    private String judgeResult;

    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
