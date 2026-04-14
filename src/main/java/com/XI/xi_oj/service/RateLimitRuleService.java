package com.XI.xi_oj.service;

import com.XI.xi_oj.mapper.RateLimitRuleMapper;
import com.XI.xi_oj.model.entity.RateLimitRule;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

public interface RateLimitRuleService extends IService<RateLimitRule> {
    /**
     * 根据 ruleKey 获取规则（优先从 Redis 缓存读取）
     */
    RateLimitRule getRuleByKey(String ruleKey);
    /**
     * 刷新指定 ruleKey 的 Redis 缓存（管理员修改后调用）
     */
    void refreshRuleCache(String ruleKey);
    /**
     * 启动时预热：将所有规则加载到 Redis 缓存
     */
    void warmUpCache();


}
