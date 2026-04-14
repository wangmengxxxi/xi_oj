package com.XI.xi_oj.model.enums;
/**
 * 限流类型枚举
 */
public enum RateLimitTypeEnum {
    /** 全局秒级限流（保护代码沙箱） */
    GLOBAL_SECOND("submit:global:second"),
    /** IP 分钟级限流（防代理刷题） */
    IP_MINUTE("submit:ip:minute"),
    /** 用户分钟级限流（防用户快速连刷） */
    USER_MINUTE("submit:user:minute"),
    /** 用户每日限流（防日级刷量） */
    USER_DAY("submit:user:day"),
    /** 用户同题冷却（防同题重复提交） */
    USER_QUESTION_COOLDOWN("submit:user:question:cooldown");
    private final String ruleKey;
    RateLimitTypeEnum(String ruleKey) {
        this.ruleKey = ruleKey;
    }
    public String getRuleKey() {
        return ruleKey;
    }
}