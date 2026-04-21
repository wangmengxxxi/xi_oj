package com.XI.xi_oj.model.dto.judge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 判题结果 DTO
 * 由 JudgeService.submitCode() 构建并返回给 OJTools.judgeUserCode()
 * 字段命名与 OJTools 调用点（getStatus/getTimeUsed/getMemoryUsed/getErrorMsg）严格对齐
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeResultDTO {
    /**
     * 判题状态（Accepted / Wrong Answer / Time Limit Exceeded / Runtime Error 等）
     * 来源：JudgeInfo.message
     */
    private String status;
    /**
     * 执行用时（ms）
     * 来源：JudgeInfo.time
     */
    private Long timeUsed;
    /**
     * 内存占用（KB）
     * 来源：JudgeInfo.memory
     */
    private Long memoryUsed;
    /**
     * 详细错误信息（非 AC 时与 status 相同；AC 时为 null）
     */
    private String errorMsg;
}