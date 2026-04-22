package com.XI.xi_oj.model.dto.question;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * WrongQuestionReviewRequest类 - 用于错题复习请求的实体类
 * 使用了Lombok的@Data注解，自动生成getter、setter、toString等方法
 */
@Data
public class WrongQuestionReviewRequest {

    @NotNull(message = "wrongQuestionId 不能为空")
    private Long wrongQuestionId;
}
