package com.XI.xi_oj.model.dto.ratelimit;

import lombok.Data;
/**
 * 限流规则更新请求
 */
@Data
public class RateLimitRuleUpdateRequest {
    /** 规则唯一键 */
    private String rule_key;
    /** 时间窗口内最大允许次数 */
    private Integer limit_count;
    /** 时间窗口大小（秒）*/
    private Integer window_seconds;
    /** 是否启用：1-启用，0-禁用 */
    private Integer is_enable;
}