package com.scriptforge.exception;

/**
 * LLM 转换异常 —— 所有重试耗尽仍失败时抛出，调用方应标记为"需人工处理"。
 */
public class ConversionException extends RuntimeException {

    public ConversionException(String message) {
        super(message);
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
