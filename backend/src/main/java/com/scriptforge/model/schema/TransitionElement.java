package com.scriptforge.model.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 转场元素 —— 每个 scene 末尾的转场信息，指定效果和下一场景 ID。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("transition")
public class TransitionElement implements ContentElement {

    /** 转场效果：切入 / 淡入 / 黑幕 / 闪回 / 无 */
    private String effect;

    /** 下一场景的 scene_id，本集最后一场为 null */
    @JsonProperty("next_scene")
    private String nextScene;
}
