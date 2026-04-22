package com.XI.xi_oj.ai.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiChatClearRequest {

    @NotBlank(message = "chatId 不能为空")
    private String chatId;
}
