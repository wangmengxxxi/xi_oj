package com.XI.xi_oj.service.impl;

import com.XI.xi_oj.ai.model.AiChatRecord;
import com.XI.xi_oj.ai.store.AiChatRecordMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiChatAsyncService {

    @Resource
    private AiChatRecordMapper chatRecordMapper;

    @Async
    public void saveRecordAsync(Long userId, String chatId, String question, String answer) {
        try {
            AiChatRecord record = new AiChatRecord();
            record.setUserId(userId);
            record.setChatId(chatId);
            record.setQuestion(question);
            record.setAnswer(answer);
            chatRecordMapper.insert(record);
        } catch (Exception e) {
            log.error("[AI Chat] async save failed, chatId={}", chatId, e);
        }
    }
}
