package com.XI.xi_oj.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

// ─────────────────────────────────────────────
// Agent 接口二：5.4 题目解析（无状态，单次会话）
// ─────────────────────────────────────────────
public interface OJQuestionParseAgent {
    @SystemMessage("""
            你是XI OJ平台的题目解析助手，负责对题目进行结构化分析：
            1. 结合提供的知识点进行考点分析，说明涉及哪些算法与数据结构；
            2. 提供分步骤解题思路，引导用户独立思考，不直接给出完整代码；
            3. 指出常见易错点与边界情况；
            4. 回答格式结构清晰，语言通俗，适配编程初学者。
            """)
    String parse(@UserMessage String questionContext);
    // SSE 流式输出
    @SystemMessage("""
            你是XI OJ平台的题目解析助手，负责对题目进行结构化分析：
            1. 结合提供的知识点进行考点分析，说明涉及哪些算法与数据结构；
            2. 提供分步骤解题思路，引导用户独立思考，不直接给出完整代码；
            3. 指出常见易错点与边界情况；
            4. 回答格式结构清晰，语言通俗，适配编程初学者。
            """)
    Flux<String> parseStream(@UserMessage String questionContext);

}
