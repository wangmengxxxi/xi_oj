package com.XI.xi_oj.model.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentAddRequest {

    @NotNull(message = "questionId 不能为空")
    private Long questionId;

    @NotBlank(message = "content 不能为空")
    private String content;

    /**
     * 根评论为 0 或 null；回复评论时传父评论 id。
     */
    private Long parentId;
}
