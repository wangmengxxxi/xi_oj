package com.XI.xi_oj.service.impl;

import com.XI.xi_oj.mapper.AiConfigMapper;
import com.XI.xi_oj.model.entity.AiConfig;
import com.XI.xi_oj.service.AiConfigService;
import com.XI.xi_oj.utils.TimeUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiConfigServiceImpl extends ServiceImpl<AiConfigMapper, AiConfig> implements AiConfigService {

    @Autowired
    private AiConfigMapper aiConfigMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CACHE_PREFIX = "ai:config:";
    private static final String NULL_PLACEHOLDER = "__NULL__";
    private static final long CACHE_TTL_MINUTES = 5;

    @Override
    public String getConfigValue(String configKey) {
        String cacheKey = CACHE_PREFIX + configKey;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return NULL_PLACEHOLDER.equals(cached) ? null : cached;
        }

        AiConfig config = aiConfigMapper.selectByConfigKey(configKey);
        if (config == null || config.getIsEnable() != 1) {
            redisTemplate.opsForValue().set(cacheKey, NULL_PLACEHOLDER, TimeUtil.minutes(CACHE_TTL_MINUTES));
            log.warn("[AiConfig] config {} is missing or disabled", configKey);
            return null;
        }

        String value = config.getConfigValue();
        redisTemplate.opsForValue().set(cacheKey, value, TimeUtil.minutes(CACHE_TTL_MINUTES));
        return value;
    }

    @Override
    public void updateConfig(String configKey, String configValue) {
        aiConfigMapper.updateValueByKey(configKey, configValue);
        redisTemplate.delete(CACHE_PREFIX + configKey);
        log.info("[AiConfig] config {} updated and cache invalidated", configKey);
    }

    @Override
    public boolean isAiEnabled() {
        String value = getConfigValue("ai.global.enable");
        return "true".equalsIgnoreCase(value);
    }

    @Override
    public String getPrompt(String promptKey, String defaultValue) {
        String value = getConfigValue(promptKey);
        if (value == null || value.isBlank()) {
            log.warn("[AiConfig] prompt {} missing, fallback to default", promptKey);
            return defaultValue;
        }
        if (looksLikeMojibake(value)) {
            log.warn("[AiConfig] prompt {} looks garbled, fallback to default", promptKey);
            return defaultValue;
        }
        return value;
    }

    /**
     * 检测常见 UTF-8/GBK 误解码后的乱码特征，避免错误 Prompt 直接进入模型。
     */
    private boolean looksLikeMojibake(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        if (text.contains("\uFFFD")) {
            return true;
        }
        String[] mojibakeMarkers = {"锛", "銆", "闂", "妫€", "鏈煡", "鏃?"};
        for (String marker : mojibakeMarkers) {
            if (text.contains(marker)) {
                return true;
            }
        }
        return false;
    }
}
