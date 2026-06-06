package com.scriptforge.model.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 单集定义 —— 包含标题和该集内的所有场景。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeDef {

    /** 集序号 */
    @JsonProperty("episode_id")
    private int episodeId;

    /** 集标题 */
    private String title;

    /** 该集下按序排列的场景列表 */
    private List<SceneDef> scenes;
}
