package com.XI.xi_oj.model.dto.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiQuestionParseResponse {

    private Long questionId;

    private String analysis;

    private List<Long> similarQuestionIds;
}
