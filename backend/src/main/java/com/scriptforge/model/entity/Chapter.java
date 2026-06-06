package com.scriptforge.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 小说章节实体 —— 从分章结果持久化到数据库 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("chapters")
public class Chapter {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("novel_id")
    private Long novelId;

    @TableField("chapter_number")
    private Integer chapterNumber;

    private String title;

    private String content;

    @TableField("word_count")
    private Integer wordCount;

    @TableField("created_at")
    private String createdAt;
}
