package com.XI.xi_oj.ai.agent;

import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

// ─────────────────────────────────────────────
// Agent 接口二：5.4 题目解析（无状态，单次会话）
// SystemMessage 由 AiModelHolder 构建时从 ai_config 动态注入
// ─────────────────────────────────────────────
public interface OJQuestionParseAgent {
    String parse(@UserMessage String questionContext);

    Flux<String> parseStream(@UserMessage String questionContext);

}
