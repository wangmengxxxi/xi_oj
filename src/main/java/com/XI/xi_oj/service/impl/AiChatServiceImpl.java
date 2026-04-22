package com.XI.xi_oj.service.impl;

import com.XI.xi_oj.ai.agent.OJChatAgent;
import com.XI.xi_oj.ai.model.AiChatHistoryPageRequest;
import com.XI.xi_oj.ai.model.AiChatHistoryPageResponse;
import com.XI.xi_oj.ai.model.AiChatRecord;
import com.XI.xi_oj.ai.store.AiChatRecordMapper;
import com.XI.xi_oj.service.AiChatService;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class AiChatServiceImpl extends ServiceImpl<AiChatRecordMapper, AiChatRecord>
        implements AiChatService {
    @Resource
    private OJChatAgent ojChatAgent;
    @Resource
    private AiChatRecordMapper chatRecordMapper;
    @Resource
    private ChatMemoryStore chatMemoryStore;

    @Override
    public String chat(String chatId, Long userId, String message) {
        String answer = ojChatAgent.chat(chatId, message);
        saveRecord(userId, chatId, message, answer);
        return answer;
    }

    @Override
    public Flux<String> chatStream(String chatId, Long userId, String message) {
        StringBuilder buffer = new StringBuilder();
        return ojChatAgent.chatStream(chatId, message)
                .doOnNext(buffer::append)
                .doOnComplete(() -> saveRecordAsync(userId, chatId, message, buffer.toString()))
                .doOnError(e -> log.error("[AI问答] 流式异常 chatId={}: {}", chatId, e.getMessage()));
    }

    @Override
    public AiChatHistoryPageResponse getChatHistoryByCursor(Long userId, AiChatHistoryPageRequest req) {
        int pageSize = Math.min(Math.max(req.getPageSize(), 1), 50);
        List<AiChatRecord> rows = chatRecordMapper.selectHistoryByCursor(
                userId, req.getChatId(), req.getCursorTime(), req.getCursorId(), pageSize);

        LocalDateTime nextCursorTime = null;
        Long nextCursorId = null;
        if (CollUtil.isNotEmpty(rows) && rows.size() == pageSize) {
            AiChatRecord tail = rows.get(rows.size() - 1);
            nextCursorTime = tail.getCreateTime();
            nextCursorId = tail.getId();
        }
        return AiChatHistoryPageResponse.of(rows, nextCursorTime, nextCursorId);
    }

    @Override
    public void clearHistory(Long userId, String chatId) {
        chatRecordMapper.deleteByUserAndChat(userId, chatId);
        chatMemoryStore.deleteMessages(chatId);
        log.info("[AI问答] 已清空会话 userId={} chatId={}", userId, chatId);
    }

    private void saveRecord(Long userId, String chatId, String question, String answer) {
        AiChatRecord record = new AiChatRecord();
        record.setUserId(userId);
        record.setChatId(chatId);
        record.setQuestion(question);
        record.setAnswer(answer);
        chatRecordMapper.insert(record);
    }

    @Async
    public void saveRecordAsync(Long userId, String chatId, String question, String answer) {
        try {
            saveRecord(userId, chatId, question, answer);
        } catch (Exception e) {
            log.error("[AI问答] 对话记录写库失败 chatId={}: {}", chatId, e.getMessage());
        }
    }
}
