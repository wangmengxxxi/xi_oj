package com.XI.xi_oj.ai.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiChatHistoryPageRequest {

    /** 会话ID（继续会话使用同一个 chatId） */
    @NotBlank(message = "chatId 不能为空")
    private String chatId;

    /** 游标时间：首次传 null，下一页传上一页返回的 nextCursorTime */
    private LocalDateTime cursorTime;

    /** 游标ID：首次传 null，下一页传上一页返回的 nextCursorId */
    private Long cursorId;

    /** 每页条数（建议 1~50） */
    private Integer pageSize = 20;
}