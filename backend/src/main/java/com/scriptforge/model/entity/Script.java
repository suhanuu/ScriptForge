package com.scriptforge.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 转换后的剧本实体，一个 novel 对应一个 script */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("scripts")
public class Script {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("novel_id")
    private Long novelId;

    @TableField("yaml_content")
    private String yamlContent;

    @TableField("scenes_count")
    private int scenesCount;

    @Builder.Default
    private String status = "CONVERTING";

    @TableField("error_message")
    private String errorMessage;

    @TableField("created_at")
    private String createdAt;

    @TableField("updated_at")
    private String updatedAt;
}
