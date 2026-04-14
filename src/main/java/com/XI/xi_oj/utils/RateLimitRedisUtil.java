package com.XI.xi_oj.utils;


import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Redis 限流工具类
 * 提供滑动窗口计数限流 和 冷却时间限流 两种能力
 */
@Component
public class RateLimitRedisUtil {
    @Resource
    private RedisTemplate<String,String> stringStringRedisTemplate;
    /**
     * 滑动窗口限流 Lua 脚本
     * 对应 USER_MINUTE、IP_MINUTE、GLOBAL_SECOND 这类时间窗口内限制次数的场景。
     * 逻辑：
     *   1. 移除窗口之外的过期元素（score < 当前时间戳 - 窗口大小）
     *   2. 统计当前窗口内的元素数量
     *   3. 若 count < maxCount，则写入当前时间戳（value=唯一UUID防重），设置key过期时间，返回1（放行）
     *   4. 否则返回0（拒绝）
     *
     * KEYS[1] = redis key
     * ARGV[1] = 当前时间戳（毫秒）
     * ARGV[2] = 窗口大小（毫秒）
     * ARGV[3] = 最大次数
     * ARGV[4] = key的TTL（秒）
     */
    private static final String SLIDING_WINDOW_SCRIPT =
            "local key = KEYS[1] " +
                    "local now = tonumber(ARGV[1]) " +
                    "local window = tonumber(ARGV[2]) " +
                    "local maxCount = tonumber(ARGV[3]) " +
                    "local ttl = tonumber(ARGV[4]) " +
                    "redis.call('ZREMRANGEBYSCORE', key, '-inf', now - window) " +
                    "local count = redis.call('ZCARD', key) " +
                    "if count < maxCount then " +
                    "  redis.call('ZADD', key, now, now .. '-' .. math.random(1, 999999)) " +
                    "  redis.call('EXPIRE', key, ttl) " +
                    "  return 1 " +
                    "else " +
                    "  return 0 " +
                    "end";
    /**
     * 冷却时间限流 Lua 脚本
     * 对应 USER_QUESTION_COOLDOWN，同一用户对同一题目的重复提交冷却。
     * 逻辑：
     *   1. 检查 key 是否存在
     *   2. 不存在则 SET key NX EX cooldownSeconds，返回1（放行）
     *   3. 存在则返回0（冷却中，拒绝）
     *
     * KEYS[1] = redis key
     * ARGV[1] = 冷却时长（秒）
     */
    private static final String COOLDOWN_SCRIPT =
            "if redis.call('EXISTS', KEYS[1]) == 0 then " +
                    "  redis.call('SET', KEYS[1], '1', 'EX', tonumber(ARGV[1])) " +
                    "  return 1 " +
                    "else " +
                    "  return 0 " +
                    "end";
    private static final DefaultRedisScript<Long> SLIDING_WINDOW_REDIS_SCRIPT;
    private static final DefaultRedisScript<Long> COOLDOWN_REDIS_SCRIPT;
    static {
        SLIDING_WINDOW_REDIS_SCRIPT = new DefaultRedisScript<>();
        SLIDING_WINDOW_REDIS_SCRIPT.setScriptText(SLIDING_WINDOW_SCRIPT);
        SLIDING_WINDOW_REDIS_SCRIPT.setResultType(Long.class);
        COOLDOWN_REDIS_SCRIPT = new DefaultRedisScript<>();
        COOLDOWN_REDIS_SCRIPT.setScriptText(COOLDOWN_SCRIPT);
        COOLDOWN_REDIS_SCRIPT.setResultType(Long.class);
    }
    /**
     * 滑动窗口限流检查
     *
     * @param redisKey      Redis Key
     * @param windowSeconds 窗口大小（秒）
     * @param maxCount      窗口内最大请求数
     * @return true=放行，false=被限流
     */
    public boolean slidingWindowAllow(String redisKey, long windowSeconds, int maxCount) {
        long now = System.currentTimeMillis();
        long windowMs = windowSeconds * 1000L;
        // TTL 设置为窗口大小的 2 倍，防止 key 提前过期导致计数丢失
        long ttlSeconds = windowSeconds * 2;
        List<String> keys = Collections.singletonList(redisKey);
        Long result = stringStringRedisTemplate.execute(
                SLIDING_WINDOW_REDIS_SCRIPT,
                keys,
                String.valueOf(now),
                String.valueOf(windowMs),
                String.valueOf(maxCount),
                String.valueOf(ttlSeconds)
        );
        return Long.valueOf(1L).equals(result);
    }
    /**
     * 冷却时间限流检查
     *
     * @param redisKey        Redis Key
     * @param cooldownSeconds 冷却时间（秒）
     * @return true=放行（不在冷却中），false=被限流（冷却中）
     */
    public boolean cooldownAllow(String redisKey, long cooldownSeconds) {
        List<String> keys = Collections.singletonList(redisKey);
        Long result = stringStringRedisTemplate.execute(
                COOLDOWN_REDIS_SCRIPT,
                keys,
                String.valueOf(cooldownSeconds)
        );
        return Long.valueOf(1L).equals(result);
    }
    /**
     * 用户每日计数限流检查（固定窗口，按自然日重置）
     *
     * @param redisKey 格式：rl:user:{userId}:submit:day:{yyyyMMdd}
     * @param maxCount 每日最大次数
     * @return true=放行，false=被限流
     */
    public boolean dailyCountAllow(String redisKey, int maxCount) {
        // INCR 原子自增，若超出 maxCount 则拒绝
        Long count = stringStringRedisTemplate.opsForValue().increment(redisKey);
        if (count == null) {
            return true;
        }
        if (count == 1L) {
            // 首次写入，设置到当天结束过期（86400秒，简化处理）
            stringStringRedisTemplate.expire(redisKey, Duration.ofSeconds(86400));
        }
        return count <= maxCount;
    }
}

