package com.XI.xi_oj.annotation;


import com.XI.xi_oj.model.enums.RateLimitTypeEnum;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    /**
     * 需要开启的限流维度列表
     * 默认同时开启：用户分钟级 + 用户每日 + 用户同题冷却
     */
    RateLimitTypeEnum[] types() default{
            RateLimitTypeEnum.USER_MINUTE,
            RateLimitTypeEnum.USER_DAY,
            RateLimitTypeEnum.USER_QUESTION_COOLDOWN
    };

    /**
     * 触发限流时的提示信息，为空时使用各维度默认提示
     */
    String message() default "";
}
