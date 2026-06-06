package com.scriptforge.model.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 剧本元信息 —— 独立于 scenes 的全局描述数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptMetadata {

    @JsonProperty("title")
    private String title;

    @JsonProperty("original_work")
    private String originalWork;

    @JsonProperty("author")
    private String author;

    @JsonProperty("version")
    @Builder.Default
    private String version = "1.0";

    @JsonProperty("created_at")
    private String createdAt;

    /** 预计时长（分钟），根据场景数自动估算 */
    @JsonProperty("estimated_duration")
    private int estimatedDuration;
}
