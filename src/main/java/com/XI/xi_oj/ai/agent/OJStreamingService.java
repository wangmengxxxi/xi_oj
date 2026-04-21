package com.XI.xi_oj.ai.agent;

import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

// ─────────────────────────────────────────────
// Agent 接口三：5.2/5.5 无状态流式输出（手动拼 Prompt 后直接推流）
// ─────────────────────────────────────────────
public interface OJStreamingService {

    // 无 @SystemMessage：Prompt 由 Service 层完整构建后传入
    // 无 @MemoryId：每次独立，无状态
    Flux<String> stream(@UserMessage String fullPrompt);
}
