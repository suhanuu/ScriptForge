package com.scriptforge.model.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 剧本根对象 —— 完整的剧本 YAML 结构体，四大顶层分块。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptOutput {

    /** 元信息：标题、作者、版本、时长等 */
    private ScriptMetadata metadata;

    /** 全局角色表，每个角色分配唯一 ID */
    private List<CharacterDef> characters;

    /** 全局场景地点表，每个地点分配唯一 ID */
    private List<LocationDef> locations;

    /** 剧集列表，每集包含若干场景 */
    private List<EpisodeDef> episodes;
}
