package com.XI.xi_oj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目评论
 */
@TableName("question_comment")
@Data
public class QuestionComment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("question_id")
    private Long questionId;

    @TableField("user_id")
    private Long userId;

    private String content;

    @TableField("parent_id")
    private Long parentId;

    @TableField("like_num")
    private Integer likeNum;

    @TableField("is_delete")
    @TableLogic
    private Integer isDelete;

    private Date createTime;

    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
