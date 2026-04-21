package com.XI.xi_oj.model.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiConfigUpdateRequest {
    String ConfigKey;
    String ConfigValue;
}
