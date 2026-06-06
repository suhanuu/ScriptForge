package com.scriptforge.client;

import com.scriptforge.config.LlmProperties;
import com.scriptforge.exception.ConversionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容 API 的 LLM 客户端实现 —— 用 Spring RestClient 发送 Chat Completions 请求。
 */
@Slf4j
@Component
public class OpenAiLlmClient implements LlmClient {

    private final LlmProperties properties;
    private final RestClient restClient;

    public OpenAiLlmClient(LlmProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));
        this.restClient = builder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(factory)
                .build();
    }

    @Override
    public boolean isConfigured() {
        return properties.isConfigured();
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        Map<String, Object> body = Map.of(
                "model", properties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "temperature", 0.3,
                "max_tokens", 8192
        );

        Exception lastException = null;
        for (int attempt = 0; attempt <= properties.getMaxRetries(); attempt++) {
            try {
                if (attempt > 0) {
                    log.info("LLM retry {}/{}", attempt, properties.getMaxRetries());
                    Thread.sleep(2000L * attempt);
                }
                String result = restClient.post()
                        .uri("/chat/completions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(String.class);
                return extractContent(result);
            } catch (Exception e) {
                lastException = e;
                log.warn("LLM call attempt {} failed: {}", attempt, e.getMessage());
            }
        }
        throw new ConversionException("LLM调用失败，已重试" + properties.getMaxRetries() + "次", lastException);
    }

    @SuppressWarnings("unchecked")
    private String extractContent(String responseBody) {
        try {
            // 简单 JSON 解析：提取 choices[0].message.content
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var root = mapper.readTree(responseBody);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new ConversionException("LLM响应解析失败: " + e.getMessage());
        }
    }
}
