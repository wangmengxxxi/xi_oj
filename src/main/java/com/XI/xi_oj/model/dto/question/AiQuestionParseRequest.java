package com.XI.xi_oj.model.dto.question;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiQuestionParseRequest {

    @NotNull(message = "questionId 不能为空")
    private Long questionId;
}
