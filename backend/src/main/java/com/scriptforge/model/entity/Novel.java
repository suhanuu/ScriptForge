package com.scriptforge.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("novels")
public class Novel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String uuid;

    @TableField("file_name")
    private String fileName;

    @TableField("raw_content")
    private String rawContent;

    @TableField("chapter_count")
    private int chapterCount;

    @TableField("total_chars")
    private int totalChars;

    @TableField("created_at")
    private String createdAt;
}
