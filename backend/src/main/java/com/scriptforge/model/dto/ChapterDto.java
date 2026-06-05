package com.scriptforge.model.dto;

/**
 * 章节信息 DTO —— 分章解析后返回的单个章节数据。
 *
 * @param index     章节序号，从 1 开始
 * @param title     章节标题，如 "初入江湖"
 * @param content   该章节的完整文本（含标题行）
 * @param charCount 内容字符数
 */
public record ChapterDto(int index, String title, String content, int charCount) {}
