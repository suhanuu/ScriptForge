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

    /**
     * 匹配中文"第X章/节/回/卷"和英文"Chapter X"格式的章节标题。
     * 支持 Markdown 标题前缀（如 ## 第一章）、纯文本标题（如 第一章 初入江湖）。
     * 限定标题长度 ≤ 50 字符，避免把正文中引用了章节号的行误判为章节标题。
     */
    private static final Pattern CHAPTER_PATTERN = Pattern.compile(
            "(?:^|\\n)[ \\t]*(?:#{1,6}[ \\t]+)?(?:第\\s*[0-9零一二三四五六七八九十百千]+\\s*[章节回卷]|Chapter\\s*\\d+)" +
                    "[ \\t]*[：:．]?[ \\t]*([^\\n]{0,50})",
            Pattern.MULTILINE
    );

    /** 兜底切分时每段的字符数 */
    private static final int FALLBACK_CHUNK_SIZE = 3000;

    /** 章节标题最大允许长度，超过此值的匹配视为正文误匹配 */
    private static final int MAX_TITLE_LENGTH = 50;

    /** 两个匹配点的最小间隔（字符数），小于此值视为同一章节的重复标题（如 Markdown 双标题） */
    private static final int MIN_GAP = 10;

    @Override
    public List<ChapterDto> split(String content) {
        Matcher matcher = CHAPTER_PATTERN.matcher(content);
        List<Integer> starts = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        while (matcher.find()) {
            String title = matcher.group(1);
            if (title == null || title.isBlank()) {
                title = "第" + (titles.size() + 1) + "章";
            }
            title = title.trim();
            if (title.length() > MAX_TITLE_LENGTH) {
                continue;
            }
            starts.add(matcher.start());
            titles.add(title);
        }

        if (starts.isEmpty()) {
            return fallbackSplit(content);
        }

        // 合并相邻太近的匹配（如 Markdown 中 ## 第一章 和 # 第一章 xxx）
        List<Integer> mergedStarts = new ArrayList<>();
        List<String> mergedTitles = new ArrayList<>();
        mergedStarts.add(starts.get(0));
        mergedTitles.add(titles.get(0));
        for (int i = 1; i < starts.size(); i++) {
            int prevStart = starts.get(i - 1);
            int currStart = starts.get(i);
            if (currStart - prevStart < MIN_GAP) {
                // 合并：用有描述性标题的那个
                String prevTitle = mergedTitles.get(mergedTitles.size() - 1);
                String currTitle = titles.get(i);
                if (!currTitle.isBlank() && (prevTitle.isBlank() || prevTitle.startsWith("第"))) {
                    mergedTitles.set(mergedTitles.size() - 1, currTitle);
                }
            } else {
                mergedStarts.add(currStart);
                mergedTitles.add(titles.get(i));
            }
        }

        List<ChapterDto> chapters = new ArrayList<>();
        for (int i = 0; i < mergedStarts.size(); i++) {
            int begin = mergedStarts.get(i);
            int end = (i + 1 < mergedStarts.size()) ? mergedStarts.get(i + 1) : content.length();
            String chapterContent = content.substring(begin, end).trim();
            String title = mergedTitles.get(i);
            if (title.isBlank()) {
                title = "第" + (i + 1) + "章";
            }

            chapters.add(new ChapterDto(
                    i + 1,
                    title,
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
