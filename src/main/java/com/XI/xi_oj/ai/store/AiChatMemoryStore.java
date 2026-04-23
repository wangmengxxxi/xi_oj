package com.XI.xi_oj.ai.store;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.XI.xi_oj.ai.model.AiChatRecord;
import com.XI.xi_oj.utils.TimeUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class AiChatMemoryStore implements ChatMemoryStore {

    private static final String MEMORY_KEY_PREFIX = "ai:chat:memory:";
    private static final int MAX_MESSAGES = 20;
    private static final int BACKFILL_ROUNDS = 10;
    private static final long MEMORY_TTL_MINUTES = 120;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private AiChatRecordMapper aiChatRecordMapper;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        MemorySession session = MemorySession.from(memoryId);
        String key = MEMORY_KEY_PREFIX + session.getRedisSuffix();

        String cached = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(cached)) {
            return ChatMessageDeserializer.messagesFromJson(cached);
        }

        List<AiChatRecord> records = loadLatestRecords(session);
        if (CollUtil.isEmpty(records)) {
            return new ArrayList<>();
        }
        Collections.reverse(records);

        List<ChatMessage> messages = new ArrayList<>(records.size() * 2);
        for (AiChatRecord record : records) {
            messages.add(UserMessage.from(record.getQuestion()));
            if (StrUtil.isNotBlank(record.getAnswer())) {
                messages.add(AiMessage.from(record.getAnswer()));
            }
        }
        saveToRedis(key, messages);
        return messages;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        MemorySession session = MemorySession.from(memoryId);
        String key = MEMORY_KEY_PREFIX + session.getRedisSuffix();

        List<ChatMessage> window = messages.size() <= MAX_MESSAGES
                ? messages
                : new ArrayList<>(messages.subList(messages.size() - MAX_MESSAGES, messages.size()));
        saveToRedis(key, window);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        MemorySession session = MemorySession.from(memoryId);
        stringRedisTemplate.delete(MEMORY_KEY_PREFIX + session.getRedisSuffix());
    }

    private List<AiChatRecord> loadLatestRecords(MemorySession session) {
        if (session.hasUserScope()) {
            return aiChatRecordMapper.selectLatestByUserAndChat(
                    session.getUserId(),
                    session.getChatId(),
                    BACKFILL_ROUNDS
            );
        }
        return aiChatRecordMapper.selectLatestByChatId(session.getChatId(), BACKFILL_ROUNDS);
    }

    private void saveToRedis(String key, List<ChatMessage> messages) {
        String json = ChatMessageSerializer.messagesToJson(messages);
        stringRedisTemplate.opsForValue().set(key, json, TimeUtil.minutes(MEMORY_TTL_MINUTES));
    }

    @Getter
    private static final class MemorySession {

        private final String redisSuffix;
        private final Long userId;
        private final String chatId;

        private MemorySession(String redisSuffix, Long userId, String chatId) {
            this.redisSuffix = redisSuffix;
            this.userId = userId;
            this.chatId = chatId;
        }

        static MemorySession from(Object memoryId) {
            String raw = String.valueOf(memoryId);
            if (StrUtil.isBlank(raw)) {
                return new MemorySession("default", null, "default");
            }

            int splitIndex = raw.indexOf(':');
            if (splitIndex > 0 && splitIndex < raw.length() - 1) {
                String userPart = raw.substring(0, splitIndex);
                String chatPart = raw.substring(splitIndex + 1);
                try {
                    Long userId = Long.parseLong(userPart);
                    if (StrUtil.isNotBlank(chatPart)) {
                        return new MemorySession(raw, userId, chatPart);
                    }
                } catch (NumberFormatException ignored) {
                    // fall through to legacy format
                }
            }
            return new MemorySession(raw, null, raw);
        }

        boolean hasUserScope() {
            return userId != null;
        }
    }
}
