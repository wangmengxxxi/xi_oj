package com.XI.xi_oj.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatHistoryPageResponse {

    /** 当前页记录（按 createTime DESC, id DESC） */
    private List<AiChatRecord> records;

    /** 下一页游标时间（无下一页时为 null） */
    private LocalDateTime nextCursorTime;

    /** 下一页游标ID（无下一页时为 null） */
    private Long nextCursorId;

    public static AiChatHistoryPageResponse of(List<AiChatRecord> records,
                                               LocalDateTime nextCursorTime,
                                               Long nextCursorId) {
        return AiChatHistoryPageResponse.builder()
                .records(records)
                .nextCursorTime(nextCursorTime)
                .nextCursorId(nextCursorId)
                .build();
    }
}