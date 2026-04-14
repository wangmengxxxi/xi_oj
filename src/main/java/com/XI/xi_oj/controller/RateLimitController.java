package com.XI.xi_oj.controller;

import com.XI.xi_oj.annotation.AuthCheck;
import com.XI.xi_oj.common.BaseResponse;
import com.XI.xi_oj.common.ErrorCode;
import com.XI.xi_oj.common.ResultUtils;
import com.XI.xi_oj.constant.UserConstant;
import com.XI.xi_oj.exception.BusinessException;
import com.XI.xi_oj.model.dto.ratelimit.RateLimitRuleUpdateRequest;
import com.XI.xi_oj.model.entity.RateLimitRule;
import com.XI.xi_oj.service.RateLimitRuleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员限流规则配置接口
 */
@RestController
@RequestMapping("/admin/rate-limit")
public class RateLimitController {
    @Resource
    private RateLimitRuleService rateLimitRuleService;
    /**
     * 获取所有限流规则
     */
    @GetMapping("/rules")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<RateLimitRule>> listRules() {
        return ResultUtils.success(rateLimitRuleService.list());
    }
    /**
     * 更新限流规则（修改后自动刷新缓存）
     */
    @PostMapping("/rule/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateRule(@RequestBody RateLimitRuleUpdateRequest updateRequest) {
        if (updateRequest == null || updateRequest.getRule_key() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 使用 MyBatis-Plus updateWrapper 按 rule_key 更新
        RateLimitRule updateEntity = new RateLimitRule();
        updateEntity.setRule_key(updateRequest.getRule_key());
        updateEntity.setLimit_count(updateRequest.getLimit_count());
        updateEntity.setWindow_seconds(updateRequest.getWindow_seconds());
        updateEntity.setIs_enable(updateRequest.getIs_enable());
        boolean success = rateLimitRuleService.update(updateEntity,
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<RateLimitRule>()
                        .eq("rule_key", updateRequest.getRule_key()));
        if (success) {
            // 刷新 Redis 缓存
            rateLimitRuleService.refreshRuleCache(updateRequest.getRule_key());
        }
        return ResultUtils.success(success);
    }
    /**
     * 重新预热所有规则缓存
     */
    @PostMapping("/cache/warm-up")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> warmUpCache() {
        rateLimitRuleService.warmUpCache();
        return ResultUtils.success("缓存预热完成");
    }
}
