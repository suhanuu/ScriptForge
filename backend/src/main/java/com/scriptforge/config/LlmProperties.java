package com.scriptforge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LLM API 配置 —— 绑定 application.yml 中 scriptforge.llm.* 配置项。
 */
@Data
@Component
@ConfigurationProperties(prefix = "scriptforge.llm")
public class LlmProperties {

    /** API Key，从环境变量 LLM_API_KEY 注入 */
    private String apiKey = "";

    /** API 基础地址，默认 DeepSeek */
    private String baseUrl = "https://api.deepseek.com/v1";

    /** 模型名称 */
    private String model = "deepseek-chat";

    /** 请求超时（秒） */
    private int timeoutSeconds = 120;

    /** LLM 调用失败最大重试次数 */
    private int maxRetries = 2;

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
