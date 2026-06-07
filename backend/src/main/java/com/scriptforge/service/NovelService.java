package com.scriptforge.service;

import com.scriptforge.exception.BusinessException;
import com.scriptforge.mapper.ChapterMapper;
import com.scriptforge.mapper.NovelMapper;
import com.scriptforge.model.dto.UploadResultDto;
import com.scriptforge.model.entity.Chapter;
import com.scriptforge.model.entity.Novel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NovelService {

    private final NovelMapper novelMapper;
    private final ChapterMapper chapterMapper;
    private final ChapterSplitter chapterSplitter;

    public UploadResultDto upload(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new BusinessException("文件名不能为空");
        }

        String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        if (!ext.equals("txt") && !ext.equals("md")) {
            throw new BusinessException("仅支持 TXT/MD 格式文件");
        }

        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BusinessException("文件读取失败: " + e.getMessage());
        }

        if (content.isBlank()) {
            throw new BusinessException("文件内容为空");
        }

        String uuid = UUID.randomUUID().toString();
        int totalChars = content.length();

        Novel novel = Novel.builder()
                .uuid(uuid)
                .fileName(originalName)
                .rawContent(content)
                .totalChars(totalChars)
                .build();

        novelMapper.insert(novel);
        var chapters = chapterSplitter.split(content);

        // 持久化章节到 DB，去除内容中的 Markdown 标题行
        for (var ch : chapters) {
            String cleanContent = ch.content()
                    .replaceFirst("^(?:#{1,6}\\s+[^\n]*\n)+", "") // 去除开头 Markdown 标题行
                    .replaceFirst("(?:\n>\\s*[^\n]*)+$", "")       // 去除末尾引用块注释
                    .trim();
            chapterMapper.insert(Chapter.builder()
                    .novelId(novel.getId())
                    .chapterNumber(ch.index())
                    .title(ch.title())
                    .content(cleanContent)
                    .wordCount(ch.charCount())
                    .build());
        }

        log.info("Uploaded novel {} (uuid={}), {} chars, {} chapters", originalName, uuid, totalChars, chapters.size());

        return new UploadResultDto(uuid, originalName, totalChars, chapters);
    }

    /** 按 UUID 查询小说 */
    public Novel getByUuid(String uuid) {
        Novel novel = novelMapper.findByUuid(uuid);
        if (novel == null) throw new BusinessException(404, "小说不存在");
        return novel;
    }

    /** 按 UUID 查询该小说的所有章节 */
    public List<Chapter> getChaptersByUuid(String uuid) {
        Novel novel = novelMapper.findByUuid(uuid);
        if (novel == null) throw new BusinessException(404, "小说不存在");
        return chapterMapper.findByNovelIdOrderByChapterNumberAsc(novel.getId());
    }
}
