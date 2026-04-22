package com.XI.xi_oj.model.vo;

import com.XI.xi_oj.model.entity.QuestionComment;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class CommentVO implements Serializable {

    private Long id;

    private Long questionId;

    private Long userId;

    private String content;

    private Long parentId;

    private Integer likeNum;

    private Date createTime;

    private List<CommentVO> replies = new ArrayList<>();

    public static CommentVO from(QuestionComment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setQuestionId(comment.getQuestionId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setParentId(comment.getParentId());
        vo.setLikeNum(comment.getLikeNum());
        vo.setCreateTime(comment.getCreateTime());
        return vo;
    }

    private static final long serialVersionUID = 1L;
}
