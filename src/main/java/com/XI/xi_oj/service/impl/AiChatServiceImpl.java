package com.XI.xi_oj.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.XI.xi_oj.ai.agent.AiModelHolder;
import com.XI.xi_oj.ai.model.AiChatHistoryPageRequest;
import com.XI.xi_oj.ai.model.AiChatHistoryPageResponse;
import com.XI.xi_oj.ai.model.AiChatRecord;
import com.XI.xi_oj.ai.store.AiChatRecordMapper;
import com.XI.xi_oj.service.AiChatService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class AiChatServiceImpl extends ServiceImpl<AiChatRecordMapper, AiChatRecord> implements AiChatService {

    @Resource
    private AiModelHolder aiModelHolder;

    @Resource
    private AiChatRecordMapper chatRecordMapper;

    @Resource
    private ChatMemoryStore chatMemoryStore;

    @Resource
    private AiChatAsyncService aiChatAsyncService;

    @Override
    public String chat(String chatId, Long userId, String message) {
        String memoryId = buildMemoryId(userId, chatId);
        String answer = aiModelHolder.getOjChatAgent().chat(memoryId, message);
        saveRecord(userId, chatId, message, answer);
        return answer;
    }

    @Override
    public Flux<String> chatStream(String chatId, Long userId, String message) {
        StringBuilder buffer = new StringBuilder();
        String memoryId = buildMemoryId(userId, chatId);
        return aiModelHolder.getOjChatAgent().chatStream(memoryId, message)
                .doOnNext(buffer::append)
                .doOnComplete(() -> aiChatAsyncService.saveRecordAsync(userId, chatId, message, buffer.toString()))
                .doOnError(e -> log.error("[AI Chat] stream failed, chatId={}", chatId, e));
    }

    @Override
    public AiChatHistoryPageResponse getChatHistoryByCursor(Long userId, AiChatHistoryPageRequest req) {
        int requestedPageSize = req.getPageSize() == null ? 20 : req.getPageSize();
        int pageSize = Math.min(Math.max(requestedPageSize, 1), 50);

        List<AiChatRecord> rows = chatRecordMapper.selectHistoryByCursor(
                userId,
                req.getChatId(),
                req.getCursorTime(),
                req.getCursorId(),
                pageSize
        );

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
    public List<AiChatRecord> getChatHistory(Long userId, String chatId) {
        return chatRecordMapper.selectByUserAndChat(userId, chatId);
    }

    @Override
    public void clearHistory(Long userId, String chatId) {
        chatRecordMapper.deleteByUserAndChat(userId, chatId);
        chatMemoryStore.deleteMessages(buildMemoryId(userId, chatId));
        // backward compatibility: clear legacy key where memoryId == chatId
        chatMemoryStore.deleteMessages(chatId);
        log.info("[AI Chat] history cleared, userId={}, chatId={}", userId, chatId);
    }

    private void saveRecord(Long userId, String chatId, String question, String answer) {
        AiChatRecord record = new AiChatRecord();
        record.setUserId(userId);
        record.setChatId(chatId);
        record.setQuestion(question);
        record.setAnswer(answer);
        chatRecordMapper.insert(record);
    }

    private String buildMemoryId(Long userId, String chatId) {
        if (userId == null) {
            return chatId;
        }
        return userId + ":" + chatId;
    }
}
