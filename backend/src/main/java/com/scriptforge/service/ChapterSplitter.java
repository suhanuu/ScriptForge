package com.scriptforge.service;

import com.scriptforge.model.dto.ChapterDto;
import java.util.List;

/** 小说分章策略接口 —— 将全文切分为有序章节列表 */
public interface ChapterSplitter {

    /**
     * 将小说全文切分为章节列表。
     * @param content 小说全文文本
     * @return 按序排列的章节列表，序号从 1 开始
     */
    List<ChapterDto> split(String content);
}
