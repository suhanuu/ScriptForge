package com.scriptforge.service;

import com.scriptforge.model.dto.ChapterDto;
import com.scriptforge.service.impl.RegexChapterSplitter;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChapterSplitterTest {

    private final ChapterSplitter splitter = new RegexChapterSplitter();

    @Test
    void shouldSplitChineseChapters() {
        String text = """
                第一章 初入江湖
                张三背着行囊走进了城门。
                街上人来人往，热闹非凡。

                第二章 危机四伏
                夜深了，张三独自走在巷子里。
                突然，一道黑影从屋顶掠过。""";

        List<ChapterDto> chapters = splitter.split(text);

        assertEquals(2, chapters.size());
        assertEquals("初入江湖", chapters.get(0).title());
        assertEquals(1, chapters.get(0).index());
        assertEquals("危机四伏", chapters.get(1).title());
        assertEquals(2, chapters.get(1).index());
        assertTrue(chapters.get(0).content().contains("张三背着行囊"));
        assertTrue(chapters.get(1).content().contains("一道黑影"));
    }

    @Test
    void shouldHandleArabicNumerals() {
        String text = """
                第1章 开始
                这是第一章的内容。

                第2章 发展
                这是第二章的内容。""";

        List<ChapterDto> chapters = splitter.split(text);

        assertEquals(2, chapters.size());
        assertEquals("开始", chapters.get(0).title());
    }

    @Test
    void shouldHandleChapterXFormat() {
        String text = """
                Chapter 1 The Beginning
                Some content here.

                Chapter 2 The Middle
                More content here.""";

        List<ChapterDto> chapters = splitter.split(text);

        assertEquals(2, chapters.size());
    }

    @Test
    void shouldFallbackWhenNoMarkers() {
        String text = "这是一段没有任何章节标记的文字。";

        List<ChapterDto> chapters = splitter.split(text);

        assertEquals(1, chapters.size());
        assertEquals("全文", chapters.get(0).title());
    }

    @Test
    void shouldCountChineseCharactersCorrectly() {
        String text = """
                第一章 测试

                这里有十个汉字的内容用于测试。""";

        List<ChapterDto> chapters = splitter.split(text);

        assertTrue(chapters.get(0).charCount() > 0);
    }

    @Test
    void shouldHandleSingleChapter() {
        String text = """
                第一章 唯一的章节
                本章是整部小说中唯一的章节。""";

        List<ChapterDto> chapters = splitter.split(text);

        assertEquals(1, chapters.size());
        assertEquals("唯一的章节", chapters.get(0).title());
    }
}
