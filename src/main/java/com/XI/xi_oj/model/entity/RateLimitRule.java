package com.XI.xi_oj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 限流规则实体
 */
@Data
@TableName("rate_limit_rule")
public class RateLimitRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 规则唯一键，对应 RateLimitTypeEnum 的 ruleKey */
    private String rule_key;
    /** 规则名称 */
    private String rule_name;
    /** 时间窗口内最大允许次数 */
    private Integer limit_count;
    /** 时间窗口大小（秒），冷却类型表示冷却时长 */
    private Integer window_seconds;
    /** 是否启用：1-启用，0-禁用 */
    private Integer is_enable;
    /** 规则描述 */
    private String description;
    private Date createTime;
    private Date updateTime;
}