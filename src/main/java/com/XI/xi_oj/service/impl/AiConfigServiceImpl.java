package com.XI.xi_oj.service.impl;

import com.XI.xi_oj.mapper.AiConfigMapper;
import com.XI.xi_oj.model.entity.AiConfig;
import com.XI.xi_oj.service.AiConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.XI.xi_oj.utils.TimeUtil;
/**
 * AI 配置服务
 * 读取逻辑：优先走 Redis 缓存（TTL 5分钟），缓存未命中回落到 MySQL，
 * 并将结果回写缓存，下次请求直接命中；修改配置时同步删除缓存，5分钟内全局生效。
 */
@Service
@Slf4j
public class AiConfigServiceImpl extends ServiceImpl<AiConfigMapper, AiConfig> implements AiConfigService {
    @Autowired
    private AiConfigMapper aiConfigMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final String CACHE_PREFIX = "ai:config:";
    /** 空值占位符,是对应的Value，防止缓存穿透 */
    private static final String NULL_PLACEHOLDER = "__NULL__";
    private static final long CACHE_TTL_MINUTES = 5;
    /**
     * 获取配置值
     * @param configKey 配置键（如 "ai.model.name"）
     * @return 配置值；配置不存在或已禁用时返回 null
     */
    @Override
    public String getConfigValue(String configKey) {
        String cacheKey = CACHE_PREFIX + configKey;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return NULL_PLACEHOLDER.equals(cached) ? null : cached;
        }
        // 缓存未命中 → 查数据库
        AiConfig config = aiConfigMapper.selectByConfigKey(configKey);
        if (config == null || config.getIsEnable() != 1) {
            // 缓存空值，防止缓存穿透（TTL 较短）
            redisTemplate.opsForValue().set(cacheKey, NULL_PLACEHOLDER,
                    TimeUtil.minutes(CACHE_TTL_MINUTES));
            log.warn("[AiConfig] 配置 {} 不存在或已禁用", configKey);
            return null;
        }
        String value = config.getConfigValue();
        redisTemplate.opsForValue().set(cacheKey, value, TimeUtil.minutes(CACHE_TTL_MINUTES));
        return value;
    }

    /**
     * 更新配置（同步删除 Redis 缓存，下次读取时自动回填）
     */
    @Override
    public void updateConfig(String configKey, String configValue) {
        aiConfigMapper.updateValueByKey(configKey, configValue);
        redisTemplate.delete(CACHE_PREFIX + configKey);
        log.info("[AiConfig] 配置 {} 已更新并刷新缓存", configKey);
    }

    /**
     * 检查 AI 功能全局开关
     * @return true = 开启，false = 关闭（含配置不存在情况）
     */
    @Override
    public boolean isAiEnabled() {
        String value = getConfigValue("ai.global.enable");
        return "true".equalsIgnoreCase(value);
    }

    /**
     * 获取 Prompt 模板（含降级兜底）
     * 供 5.2/5.4/5.5 各模块 Service 层调用，避免 Prompt 硬编码在 Java 代码中。
     * 管理员在后台修改 ai_config 对应行后，5分钟内全局生效，无需重启。
     *
     * @param promptKey    配置键，如 "ai.prompt.code_analysis"
     * @param defaultValue 降级默认值（配置不存在或 is_enable=0 时使用）
     * @return Prompt 字符串
     */
    @Override
    public String getPrompt(String promptKey, String defaultValue) {
        String value = getConfigValue(promptKey);
        if (value == null || value.isBlank()) {
            log.warn("[AiConfig] Prompt {} 未配置，使用默认值", promptKey);
            return defaultValue;
        }
        return value;
    }
}
