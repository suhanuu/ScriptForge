package com.scriptforge.model.dto;

import java.util.List;

/**
 * 小说上传结果 DTO。
 *
 * @param novelId    小说唯一标识（UUID）
 * @param fileName   原始文件名
 * @param totalChars 总字符数
 * @param chapters   分章结果列表
 */
public record UploadResultDto(String novelId, String fileName, int totalChars, List<ChapterDto> chapters) {}
