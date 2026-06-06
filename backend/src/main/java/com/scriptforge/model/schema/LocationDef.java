package com.scriptforge.model.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局场景地点定义 —— 归一化不同写法（"废弃仓库"/"旧仓库"），唯一 ID 供 scene 引用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDef {

    /** 地点唯一标识，如 "apt_linyuan" */
    private String id;

    /** 地点名称 */
    private String name;

    /** 类型：内景 / 外景 */
    private String type;

    /** 地点简短描述 */
    private String description;
}
