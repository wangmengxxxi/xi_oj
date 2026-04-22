package com.XI.xi_oj.service;

import com.XI.xi_oj.ai.model.AiChatHistoryPageRequest;
import com.XI.xi_oj.ai.model.AiChatHistoryPageResponse;
import reactor.core.publisher.Flux;

public interface AiChatService {

    /**
     * 非流式问答 + 同步写库
     */
    public String chat(String chatId, Long userId, String message);

    /**
     * SSE 流式问答 + 异步写库
     * doOnComplete：流结束后写入 MySQL，Redis 记忆由 ChatMemoryStore 自动更新
     */
    public Flux<String> chatStream(String chatId, Long userId, String message);

    /**
     * 游标分页查询历史记录：首次 cursorTime/cursorId 传 null
     */
    public AiChatHistoryPageResponse getChatHistoryByCursor(Long userId, AiChatHistoryPageRequest req);
    /**
     * 清空会话历史：同时清 MySQL + Redis 记忆
     */
    public void clearHistory(Long userId, String chatId);
}
