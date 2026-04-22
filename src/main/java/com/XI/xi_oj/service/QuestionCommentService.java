package com.XI.xi_oj.service;

import com.XI.xi_oj.model.entity.QuestionComment;
import com.XI.xi_oj.model.vo.CommentVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface QuestionCommentService extends IService<QuestionComment> {

    Long addComment(Long questionId, Long userId, String content, Long parentId);

    List<CommentVO> getCommentTree(Long questionId);

    boolean toggleLike(Long commentId, Long userId);

    void deleteComment(Long commentId, Long userId, boolean isAdmin);
}
