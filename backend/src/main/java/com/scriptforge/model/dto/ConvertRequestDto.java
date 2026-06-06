package com.scriptforge.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 剧本转换请求 DTO。
 * @param novelUuid 小说 UUID
 * @param chapterNumbers 要转换的章节序号列表，null 或空表示全部转换
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvertRequestDto {

    @NotNull(message = "novelUuid 不能为空")
    private String novelUuid;

    private List<Integer> chapterNumbers;
}
