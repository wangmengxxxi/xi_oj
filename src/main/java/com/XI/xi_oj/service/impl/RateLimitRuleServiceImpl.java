package com.XI.xi_oj.service.impl;

import cn.hutool.json.JSONUtil;
import com.XI.xi_oj.mapper.RateLimitRuleMapper;
import com.XI.xi_oj.model.entity.RateLimitRule;
import com.XI.xi_oj.service.RateLimitRuleService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class RateLimitRuleServiceImpl extends ServiceImpl<RateLimitRuleMapper,RateLimitRule> implements RateLimitRuleService {
    /** Redis 缓存的限流规则 key 前缀 */
    private static final String RULE_CACHE_PREFIX = "rl:rule:";
    /** 规则缓存 TTL：5 分钟 *
     *  否则规则会越堆积越多，否则管理员修改了规则，旧规则任然会存在
     *  加上TTL后，旧规则最多存在五分钟
     */

    private static final Duration RULE_CACHE_TTL = Duration.ofMinutes(5);
    @Resource
    private RedisTemplate<String, String> stringStringRedisTemplate;
    @PostConstruct
    @Override
    public void warmUpCache() {
        List<RateLimitRule> rules = list();
        for (RateLimitRule rule : rules) {
            String cacheKey = RULE_CACHE_PREFIX + rule.getRule_key();
            stringStringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(rule), RULE_CACHE_TTL);
        }
        log.info("[RateLimit] 限流规则缓存预热完成，共 {} 条规则", rules.size());
    }


    @Override
    public RateLimitRule getRuleByKey(String ruleKey) {
        String cacheKey = RULE_CACHE_PREFIX + ruleKey;
        String cached = stringStringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return JSONUtil.toBean(cached, RateLimitRule.class);
        }
        // 缓存未命中，从 DB 查询
        RateLimitRule rule = getOne(new QueryWrapper<RateLimitRule>().eq("rule_key", ruleKey));
        if (rule != null) {
            stringStringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(rule), RULE_CACHE_TTL);
        }
        return rule;
    }
    @Override
    public void refreshRuleCache(String ruleKey) {
        String cacheKey = RULE_CACHE_PREFIX + ruleKey;
        stringStringRedisTemplate.delete(cacheKey);
        getRuleByKey(ruleKey); // 触发重新加载
        log.info("[RateLimit] 规则缓存已刷新：{}", ruleKey);
    }
}
