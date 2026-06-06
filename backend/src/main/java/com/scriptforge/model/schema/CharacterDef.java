package com.scriptforge.model.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局角色定义 —— 分配唯一 ID，跨章节统一引用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterDef {

    /** 角色唯一标识，如 "lin_yuan" */
    private String id;

    /** 角色名称 */
    private String name;

    /** 角色定位：主角 / 配角 / 反派 / 路人 */
    private String role;

    /** 角色简短描述 */
    private String description;
}
