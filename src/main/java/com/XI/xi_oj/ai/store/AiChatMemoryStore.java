package com.XI.xi_oj.ai.store;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.XI.xi_oj.ai.model.AiChatRecord;
import com.XI.xi_oj.utils.TimeUtil;
import dev.langchain4j.data.message.*;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Component
@Slf4j
public class AiChatMemoryStore implements ChatMemoryStore {

    private static final String MEMORY_KEY_PREFIX = "ai:chat:memory:";
    private static final int MAX_MESSAGES = 20;        // 与 MessageWindowChatMemory 保持一致
    private static final int BACKFILL_ROUNDS = 10;     // 1轮=1问1答，10轮≈20条消息
    private static final long MEMORY_TTL_MINUTES = 120;

    @Resource
    private StringRedisTemplate redisTemplate;
    @Resource
    private AiChatRecordMapper aiChatRecordMapper;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String chatId = String.valueOf(memoryId);
        String key = MEMORY_KEY_PREFIX + chatId;

        // 1) 先查 Redis
        String cached = redisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(cached)) {
            return ChatMessageDeserializer.messagesFromJson(cached);
        }

        // 2) Redis miss：从 MySQL 回源（最近 N 轮），并回填 Redis
        List<AiChatRecord> records = aiChatRecordMapper.selectLatestByChatId(chatId, BACKFILL_ROUNDS);
        if (CollUtil.isEmpty(records)) {
            return new ArrayList<>();
        }
        Collections.reverse(records); // DB 是倒序查出，反转后恢复时间正序

        List<ChatMessage> messages = new ArrayList<>(records.size() * 2);
        for (AiChatRecord r : records) {
            messages.add(UserMessage.from(r.getQuestion()));
            if (StrUtil.isNotBlank(r.getAnswer())) {
                messages.add(AiMessage.from(r.getAnswer()));
            }
        }
        saveToRedis(key, messages);
        return messages;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String key = MEMORY_KEY_PREFIX + memoryId;

        // 双保险：仅保留最后 MAX_MESSAGES 条
        List<ChatMessage> window = messages.size() <= MAX_MESSAGES
                ? messages
                : new ArrayList<>(messages.subList(messages.size() - MAX_MESSAGES, messages.size()));

        saveToRedis(key, window);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        redisTemplate.delete(MEMORY_KEY_PREFIX + memoryId);
    }

    private void saveToRedis(String key, List<ChatMessage> messages) {
        String json = ChatMessageSerializer.messagesToJson(messages);
        redisTemplate.opsForValue().set(key, json, TimeUtil.minutes(MEMORY_TTL_MINUTES));
    }
}
