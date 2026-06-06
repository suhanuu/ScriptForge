package com.scriptforge.model.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 单场戏 —— 同一地点、同一时间段内发生的一系列动作和对白。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneDef {

    /** 场景编号，格式 "集-场"，如 "1-1" */
    @JsonProperty("scene_id")
    private String sceneId;

    /** 地点 ID（引用 locations[].id） */
    private String location;

    /** 时间段，如 "深夜"、"白天" */
    private String time;

    /** 天气，如 "雨"、"晴" */
    private String weather;

    /** 本场出现角色的 ID 列表 */
    @JsonProperty("characters_present")
    private List<String> charactersPresent;

    /** 按时间线排列的多态内容元素（action / dialogue / transition） */
    private List<ContentElement> content;
}
