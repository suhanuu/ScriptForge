package com.scriptforge.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SfResult<T>(int code, String message, T data) {

    public static <T> SfResult<T> success(T data) {
        return new SfResult<>(0, "success", data);
    }

    public static <T> SfResult<T> success() {
        return new SfResult<>(0, "success", null);
    }

    public static <T> SfResult<T> error(int code, String message) {
        return new SfResult<>(code, message, null);
    }
}
