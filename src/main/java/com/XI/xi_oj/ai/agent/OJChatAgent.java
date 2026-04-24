package com.XI.xi_oj.ai.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

// ─────────────────────────────────────────────
// Agent 接口一：5.3 AI问答（有状态，多轮对话）
// SystemMessage 由 AiModelHolder 构建时从 ai_config 动态注入
// ─────────────────────────────────────────────
public interface OJChatAgent {
    String chat(@MemoryId String chatId, @UserMessage String userQuery);

    Flux<String> chatStream(@MemoryId String chatId, @UserMessage String userQuery);

}
