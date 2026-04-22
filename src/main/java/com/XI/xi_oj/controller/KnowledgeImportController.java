package com.XI.xi_oj.controller;

import com.XI.xi_oj.ai.rag.KnowledgeInitializer;
import com.XI.xi_oj.annotation.AuthCheck;
import com.XI.xi_oj.common.BaseResponse;
import com.XI.xi_oj.common.ErrorCode;
import com.XI.xi_oj.common.ResultUtils;
import com.XI.xi_oj.constant.UserConstant;
import com.XI.xi_oj.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/admin/knowledge")
@Slf4j
public class KnowledgeImportController {

    @Resource
    private KnowledgeInitializer knowledgeInitializer;

    @PostMapping("/import")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> importKnowledge(@RequestPart("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        String filename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(filename);
        if (extension == null || !("md".equalsIgnoreCase(extension) || "markdown".equalsIgnoreCase(extension))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仅支持导入 .md / .markdown 文件");
        }
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        int importedCount = knowledgeInitializer.parseAndStore(content);
        knowledgeInitializer.validateImportedCount(importedCount);
        log.info("[Knowledge Import] admin imported knowledge file={}, count={}", filename, importedCount);
        return ResultUtils.success("成功导入 " + importedCount + " 条知识条目");
    }
}
