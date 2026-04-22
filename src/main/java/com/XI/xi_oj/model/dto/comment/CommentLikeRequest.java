package com.XI.xi_oj.model.dto.comment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentLikeRequest {

    @NotNull(message = "commentId 不能为空")
    private Long commentId;
}
