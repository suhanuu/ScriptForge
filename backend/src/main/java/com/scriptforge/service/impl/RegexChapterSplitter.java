package com.scriptforge.service.impl;

import com.scriptforge.model.dto.ChapterDto;
import com.scriptforge.service.ChapterSplitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则分章解析器 —— 用正则匹配"第X章""Chapter X"等模式识别章节边界。
 * 匹配不到时走兜底策略：先尝试按空行切分，再不行按 3000 字等长切分。
 */
@Slf4j
@Component
public class RegexChapterSplitter implements ChapterSplitter {

    /** 匹配中文"第X章/节/回/卷"和英文"Chapter X"格式的章节标题 */
    private static final Pattern CHAPTER_PATTERN = Pattern.compile(
            "(?:^|\\n)\\s*(?:第\\s*[0-9零一二三四五六七八九十百千]+\\s*[章节回卷]|Chapter\\s*\\d+|第[0-9零一二三四五六七八九十百千]+[章节])" +
                    "\\s*[：:．\\s]?\\s*([^\\n]*)",
            Pattern.MULTILINE
    );

    /** 兜底切分时每段的字符数 */
    private static final int FALLBACK_CHUNK_SIZE = 3000;

    @Override
    public List<ChapterDto> split(String content) {
        Matcher matcher = CHAPTER_PATTERN.matcher(content);
        List<Integer> starts = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        while (matcher.find()) {
            starts.add(matcher.start());
            String title = matcher.group(1);
            if (title == null || title.isBlank()) {
                title = "第" + (titles.size() + 1) + "章";
            }
            titles.add(title.trim());
        }

        if (starts.isEmpty()) {
            return fallbackSplit(content);
        }

        List<ChapterDto> chapters = new ArrayList<>();
        for (int i = 0; i < starts.size(); i++) {
            int begin = starts.get(i);
            int end = (i + 1 < starts.size()) ? starts.get(i + 1) : content.length();
            String chapterContent = content.substring(begin, end).trim();

            chapters.add(new ChapterDto(
                    i + 1,
                    titles.get(i),
                    chapterContent,
                    chapterContent.length()
            ));
        }

        log.info("Split into {} chapters", chapters.size());
        return chapters;
    }

    private List<ChapterDto> fallbackSplit(String content) {
        // Try splitting by consecutive blank lines first
        String[] paragraphs = content.split("\\n{2,}");
        if (paragraphs.length >= 3) {
            List<ChapterDto> chapters = new ArrayList<>();
            for (int i = 0; i < paragraphs.length; i++) {
                String text = paragraphs[i].trim();
                if (text.isBlank()) continue;
                String title = text.lines().findFirst().orElse("第" + (i + 1) + "章").trim();
                if (title.length() > 50) {
                    title = title.substring(0, 50);
                }
                chapters.add(new ChapterDto(chapters.size() + 1, title, text, text.length()));
            }
            if (chapters.size() >= 2) {
                log.info("Fallback split by blank lines: {} chapters", chapters.size());
                return chapters;
            }
        }

        // Last resort: split by character count
        String trimmed = content.trim();
        if (trimmed.length() <= FALLBACK_CHUNK_SIZE) {
            log.info("Fallback: single chapter");
            return List.of(new ChapterDto(1, "全文", trimmed, trimmed.length()));
        }

        List<ChapterDto> chapters = new ArrayList<>();
        int idx = 0;
        while (idx < trimmed.length()) {
            int end = Math.min(idx + FALLBACK_CHUNK_SIZE, trimmed.length());
            String chunk = trimmed.substring(idx, end).trim();
            if (!chunk.isBlank()) {
                chapters.add(new ChapterDto(chapters.size() + 1, "第" + (chapters.size() + 1) + "段", chunk, chunk.length()));
            }
            idx = end;
        }
        log.info("Fallback split by size: {} chunks", chapters.size());
        return chapters;
    }
}
