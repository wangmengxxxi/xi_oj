package com.XI.xi_oj.aop;

import com.XI.xi_oj.common.ErrorCode;
import com.XI.xi_oj.exception.BusinessException;
import com.XI.xi_oj.service.AiConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class AiGlobalSwitchAspect {

    @Resource
    private AiConfigService aiConfigService;

    @Pointcut("execution(public * com.XI.xi_oj.controller.Ai*Controller.*(..)) && !within(com.XI.xi_oj.controller.AiConfigController)")
    public void aiControllerMethods() {
    }

    @Before("aiControllerMethods()")
    public void checkAiSwitch(JoinPoint joinPoint) {
        if (!aiConfigService.isAiEnabled()) {
            log.info("[AI Switch] blocked request: {}", joinPoint.getSignature().toShortString());
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "AI 功能当前已关闭，请联系管理员开启");
        }
    }
}
