package com.scriptforge.client;

/**
 * LLM 调用客户端接口 —— 抽象 OpenAI 兼容 API 的调用逻辑。
 */
public interface LlmClient {

    /** API Key 是否已配置 */
    boolean isConfigured();

    /**
     * 发送 Chat Completions 请求。
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @return LLM 返回的文本内容
     */
    String chat(String systemPrompt, String userMessage);
}
