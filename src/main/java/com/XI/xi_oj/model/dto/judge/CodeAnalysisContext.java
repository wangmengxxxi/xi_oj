package com.XI.xi_oj.model.dto.judge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeAnalysisContext {

    private Long questionId;

    private String title;

    private String content;

    private String tags;

    private String difficulty;

    private String answer;

    private String userCode;

    private String language;

    private String judgeStatus;

    private String errorMsg;

    private Long userId;
}
