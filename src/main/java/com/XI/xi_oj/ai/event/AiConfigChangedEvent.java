package com.XI.xi_oj.ai.event;

import org.springframework.context.ApplicationEvent;

public class AiConfigChangedEvent extends ApplicationEvent {

    private final String configKey;

    public AiConfigChangedEvent(Object source, String configKey) {
        super(source);
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }
}
