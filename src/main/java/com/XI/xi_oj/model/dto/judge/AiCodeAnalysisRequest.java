package com.XI.xi_oj.model.dto.judge;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiCodeAnalysisRequest {

    @NotNull(message = "questionId 不能为空")
    private Long questionId;

    /**
     * 可选：若传入则优先基于历史提交记录构建分析上下文（推荐）。
     */
    private Long questionSubmitId;

    /**
     * 不传 questionSubmitId 时必填。
     */
    private String code;

    /**
     * 不传 questionSubmitId 时必填。
     */
    private String language;

    /**
     * 可选：判题状态（例如 Accepted / Wrong Answer / Runtime Error）。
     */
    private String judgeStatus;

    /**
     * 可选：判题错误信息。
     */
    private String errorMsg;
}
