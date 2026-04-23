package com.XI.xi_oj.controller;

import com.XI.xi_oj.ai.rag.QuestionVectorSyncService;
import com.XI.xi_oj.annotation.AuthCheck;
import com.XI.xi_oj.common.BaseResponse;
import com.XI.xi_oj.common.ErrorCode;
import com.XI.xi_oj.common.ResultUtils;
import com.XI.xi_oj.exception.BusinessException;
import com.XI.xi_oj.model.dto.ai.AiConfigUpdateRequest;
import com.XI.xi_oj.service.AiConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/ai")
@Slf4j
public class AiConfigController {
    @Autowired
    private AiConfigService aiConfigService;

    @Autowired
    private QuestionVectorSyncService questionVectorSyncService;
    /**
     * 可读写的配置 Key 白名单（含模型参数、RAG 参数、各模块 Prompt）
     * api_key 不在此列，统一走环境变量注入
     */
    private static final List<String> READABLE_KEYS = Arrays.asList(
            "ai.global.enable",
            "ai.model.name", "ai.model.base_url", "ai.model.embedding_name",
            "ai.rag.top_k", "ai.rag.similarity_threshold",
            // Prompt 动态管理配置项（对应 5.2 代码分析 / 5.4 题目解析 / 5.5 错题分析）
            "ai.prompt.code_analysis", "ai.prompt.wrong_analysis", "ai.prompt.question_parse"
    );
    /** 获取所有可读 AI 配置（过滤敏感项） */
    @GetMapping("/config")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Map<String, String>> getConfig() {
        Map<String, String> result = new LinkedHashMap<>();
        for (String key : READABLE_KEYS) {
            result.put(key, aiConfigService.getConfigValue(key));
        }
        return ResultUtils.success(result);
    }
    /** 修改 AI 配置（禁止修改 api_key，统一走环境变量） */
    @PostMapping("/config")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<String> updateConfig(@RequestBody AiConfigUpdateRequest request) {
        if ("ai.model.api_key".equals(request.getConfigKey())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,
                    "API Key 不允许通过接口修改，请使用环境变量 AI_API_KEY");
        }
        aiConfigService.updateConfig(request.getConfigKey(), request.getConfigValue());
        return ResultUtils.success("配置更新成功，模型与 RAG 参数即时重建生效，Prompt 类配置最多 5 分钟内生效");
    }

    @PostMapping("/question-vector/rebuild")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<String> rebuildQuestionVectors() {
        int count = questionVectorSyncService.rebuildQuestionVectors();
        return ResultUtils.success("题目向量重建完成，成功同步 " + count + " 道题目");
    }
}