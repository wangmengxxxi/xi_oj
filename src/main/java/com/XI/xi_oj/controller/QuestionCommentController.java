package com.XI.xi_oj.controller;

import com.XI.xi_oj.common.BaseResponse;
import com.XI.xi_oj.common.ResultUtils;
import com.XI.xi_oj.model.dto.comment.CommentAddRequest;
import com.XI.xi_oj.model.dto.comment.CommentDeleteRequest;
import com.XI.xi_oj.model.dto.comment.CommentLikeRequest;
import com.XI.xi_oj.model.entity.User;
import com.XI.xi_oj.model.vo.CommentVO;
import com.XI.xi_oj.service.QuestionCommentService;
import com.XI.xi_oj.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class QuestionCommentController {

    private final QuestionCommentService questionCommentService;

    private final UserService userService;

    public QuestionCommentController(QuestionCommentService questionCommentService, UserService userService) {
        this.questionCommentService = questionCommentService;
        this.userService = userService;
    }

    @PostMapping("/add")
    public BaseResponse<Long> addComment(@RequestBody @Valid CommentAddRequest request, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        Long commentId = questionCommentService.addComment(
                request.getQuestionId(),
                loginUser.getId(),
                request.getContent(),
                request.getParentId()
        );
        return ResultUtils.success(commentId);
    }

    @GetMapping("/list")
    public BaseResponse<List<CommentVO>> listComments(@RequestParam Long questionId) {
        return ResultUtils.success(questionCommentService.getCommentTree(questionId));
    }

    @PostMapping("/like")
    public BaseResponse<Boolean> toggleLike(@RequestBody @Valid CommentLikeRequest request, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        boolean liked = questionCommentService.toggleLike(request.getCommentId(), loginUser.getId());
        return ResultUtils.success(liked);
    }

    @PostMapping("/delete")
    public BaseResponse<String> deleteComment(@RequestBody @Valid CommentDeleteRequest request,
                                              HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        boolean isAdmin = userService.isAdmin(loginUser);
        questionCommentService.deleteComment(request.getCommentId(), loginUser.getId(), isAdmin);
        return ResultUtils.success("删除成功");
    }
}
