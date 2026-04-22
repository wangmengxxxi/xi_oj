package com.XI.xi_oj.controller;

import com.XI.xi_oj.ai.agent.OJChatAgent;
import com.XI.xi_oj.ai.model.AiChatRequest;
import com.XI.xi_oj.annotation.RateLimit;
import static com.XI.xi_oj.model.enums.RateLimitTypeEnum.*;
import com.XI.xi_oj.common.BaseResponse;
import com.XI.xi_oj.common.ResultUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

// ─────────────────────────────────────────────
// SSE Controller 公共示例（以 5.3 AI问答为代表）
// ─────────────────────────────────────────────
// 【SSE Token 空格/换行丢失问题说明】
// LLM 输出的 token 可能以空格开头（如 " hello"）或包含 \n 换行符。
// 若直接写入 SSE 的 data 字段：
//   data:  hello      ← 两个空格，部分客户端解析为一个空格后再 trim，空格丢失
//   data: line1\nline2 ← \n 会被 SSE 协议解释为帧分隔符，直接破坏帧结构
// 解决方案：将每个 token 封装为 JSON { "d": "<token>" }，由前端解析 JSON 取值。
// 前端接收示例：
//   eventSource.onmessage = (e) => {
//     const { d } = JSON.parse(e.data);
//     output += d;   // 空格、换行、特殊字符全部安全保留
//   };
// ─────────────────────────────────────────────
@RestController
@RequestMapping("/ai")
public class AiChatController {

    @Autowired
    private OJChatAgent ojChatAgent;
    @Autowired
    private ObjectMapper objectMapper;  // Spring 自动注入，用于 JSON 序列化

    /**
     * 非流式接口：完整回答一次性返回
     */
    @RateLimit(types = {AI_USER_MINUTE, AI_IP_MINUTE, AI_CHAT_USER_DAY})
    @PostMapping("/chat")
    public BaseResponse<String> chat(@RequestBody AiChatRequest request, HttpServletRequest httpRequest) {
        String result = ojChatAgent.chat(request.getChatId(), request.getMessage());
        return ResultUtils.success(result);
    }

    /**
     * SSE 流式接口：每个 token 封装为 JSON {"d":"<token>"} 后推送
     * 前端通过 EventSource 接收，解析 JSON 拼接完整文本
     */
    @RateLimit(types = {AI_USER_MINUTE, AI_IP_MINUTE, AI_CHAT_USER_DAY})
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(@RequestParam String chatId,
                                                    @RequestParam String message,
                                                    HttpServletRequest httpRequest) {
        return ojChatAgent.chatStream(chatId, message)
                .map(token -> {
                    try {
                        // 封装为 JSON，保留空格、换行、特殊字符
                        String json = objectMapper.writeValueAsString(Map.of("d", token));
                        return ServerSentEvent.<String>builder().data(json).build();
                    } catch (Exception e) {
                        return ServerSentEvent.<String>builder().data("{\"d\":\"\"}").build();
                    }
                })
                // 结束信号：前端收到后关闭 EventSource 连接
                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                        .data("{\"done\":true}")
                        .build()))
                .onErrorResume(e -> Flux.just(ServerSentEvent.<String>builder()
                        .event("error")
                        .data("{\"error\":\"" + e.getMessage() + "\"}")
                        .build()));
    }
}
