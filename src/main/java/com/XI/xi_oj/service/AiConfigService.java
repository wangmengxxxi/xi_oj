package com.XI.xi_oj.service;

import com.XI.xi_oj.model.entity.AiConfig;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


public interface AiConfigService extends IService<AiConfig>{

    /**
     * 获取配置值
     * @param configKey 配置键（如 "ai.model.name"）
     * @return 配置值；配置不存在或已禁用时返回 null
     */
    public String getConfigValue(String configKey);


    /**
     * 更新配置（同步删除 Redis 缓存，下次读取时自动回填）
     */
    public void updateConfig(String configKey, String configValue);

    /**
     * 检查 AI 功能全局开关
     * @return true = 开启，false = 关闭（含配置不存在情况）
     */
    public boolean isAiEnabled();

    /**
     * 获取 Prompt 模板（含降级兜底）
     * 供 5.2/5.4/5.5 各模块 Service 层调用，避免 Prompt 硬编码在 Java 代码中。
     * 管理员在后台修改 ai_config 对应行后，5分钟内全局生效，无需重启。
     *
     * @param promptKey    配置键，如 "ai.prompt.code_analysis"
     * @param defaultValue 降级默认值（配置不存在或 is_enable=0 时使用）
     * @return Prompt 字符串
     */
    public String getPrompt(String promptKey, String defaultValue);

}
