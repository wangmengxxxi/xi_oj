package com.XI.xi_oj.service.impl;

import com.XI.xi_oj.common.ErrorCode;
import com.XI.xi_oj.exception.BusinessException;
import com.XI.xi_oj.mapper.QuestionCommentMapper;
import com.XI.xi_oj.model.entity.Question;
import com.XI.xi_oj.model.entity.QuestionComment;
import com.XI.xi_oj.model.vo.CommentVO;
import com.XI.xi_oj.service.QuestionCommentService;
import com.XI.xi_oj.service.QuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class QuestionCommentServiceImpl extends ServiceImpl<QuestionCommentMapper, QuestionComment>
        implements QuestionCommentService {

    private static final String COMMENT_LIKE_KEY_PREFIX = "comment:like:";

    @Resource
    private QuestionCommentMapper commentMapper;

    @Resource
    private QuestionService questionService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Long addComment(Long questionId, Long userId, String content, Long parentId) {
        Question question = questionService.getById(questionId);
        if (question == null || Objects.equals(question.getIsDelete(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容不能为空");
        }

        long targetParentId = parentId == null ? 0L : parentId;
        if (targetParentId != 0L) {
            QuestionComment parent = commentMapper.selectById(targetParentId);
            if (parent == null || Objects.equals(parent.getIsDelete(), 1)) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "父评论不存在");
            }
            if (!Objects.equals(parent.getQuestionId(), questionId)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "父评论不属于当前题目");
            }
        }

        QuestionComment comment = new QuestionComment();
        comment.setQuestionId(questionId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        comment.setParentId(targetParentId);
        comment.setLikeNum(0);
        comment.setIsDelete(0);
        boolean saved = this.save(comment);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "评论发布失败");
        }
        return comment.getId();
    }

    @Override
    public List<CommentVO> getCommentTree(Long questionId) {
        List<QuestionComment> allComments = commentMapper.selectByQuestionId(questionId);
        if (allComments == null || allComments.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, CommentVO> idToVo = new LinkedHashMap<>();
        List<CommentVO> roots = new ArrayList<>();
        for (QuestionComment comment : allComments) {
            CommentVO vo = CommentVO.from(comment);
            idToVo.put(comment.getId(), vo);
            if (comment.getParentId() == null || comment.getParentId() == 0L) {
                roots.add(vo);
            }
        }

        for (QuestionComment comment : allComments) {
            Long pid = comment.getParentId();
            if (pid != null && pid != 0L) {
                CommentVO parent = idToVo.get(pid);
                CommentVO self = idToVo.get(comment.getId());
                if (parent != null && self != null) {
                    parent.getReplies().add(self);
                }
            }
        }

        roots.sort(Comparator.comparingInt((CommentVO c) -> c.getLikeNum() == null ? 0 : c.getLikeNum()).reversed()
                .thenComparing(CommentVO::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(CommentVO::getId, Comparator.nullsLast(Comparator.naturalOrder())));

        for (CommentVO root : roots) {
            sortRepliesRecursively(root);
        }
        return roots;
    }

    private void sortRepliesRecursively(CommentVO node) {
        node.getReplies().sort(Comparator.comparing(CommentVO::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(CommentVO::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        for (CommentVO reply : node.getReplies()) {
            sortRepliesRecursively(reply);
        }
    }

    @Override
    public boolean toggleLike(Long commentId, Long userId) {
        QuestionComment comment = commentMapper.selectById(commentId);
        if (comment == null || Objects.equals(comment.getIsDelete(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }

        String likeKey = COMMENT_LIKE_KEY_PREFIX + commentId;
        String userMember = String.valueOf(userId);
        Long removed = stringRedisTemplate.opsForSet().remove(likeKey, userMember);
        if (removed != null && removed > 0) {
            commentMapper.decrementLike(commentId);
            return false;
        }

        Long added = stringRedisTemplate.opsForSet().add(likeKey, userMember);
        if (added != null && added > 0) {
            commentMapper.incrementLike(commentId);
        }
        return true;
    }

    @Override
    public void deleteComment(Long commentId, Long userId, boolean isAdmin) {
        QuestionComment comment = commentMapper.selectById(commentId);
        if (comment == null || Objects.equals(comment.getIsDelete(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }
        if (!isAdmin && !Objects.equals(comment.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除该评论");
        }
        comment.setIsDelete(1);
        boolean updated = this.updateById(comment);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除评论失败");
        }
    }
}
