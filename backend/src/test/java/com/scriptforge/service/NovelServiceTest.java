package com.scriptforge.service;

import com.scriptforge.exception.BusinessException;
import com.scriptforge.model.entity.Novel;
import com.scriptforge.mapper.NovelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NovelServiceTest {

    @Autowired
    private NovelService novelService;

    @Autowired
    private NovelMapper novelMapper;

    @BeforeEach
    void setUp() {
        // clean test data — ignore if table doesn't exist yet
        try {
            novelMapper.delete(null);
        } catch (Exception ignored) {
        }
    }

    @Test
    void shouldUploadValidTxtFile() {
        var file = new MockMultipartFile(
                "file", "故事.txt", "text/plain",
                "第一章 开始\n这是测试内容，有足够的中文字符来通过验证。".getBytes()
        );

        var result = novelService.upload(file);

        assertNotNull(result.novelId());
        assertFalse(result.novelId().isBlank());
        assertEquals("故事.txt", result.fileName());
        assertTrue(result.totalChars() > 0);
    }

    @Test
    void shouldRejectEmptyContent() {
        var file = new MockMultipartFile(
                "file", "空文件.txt", "text/plain", "   \n  ".getBytes()
        );

        var ex = assertThrows(BusinessException.class, () -> novelService.upload(file));
        assertEquals("文件内容为空", ex.getMessage());
    }

    @Test
    void shouldRejectExeFile() {
        var file = new MockMultipartFile(
                "file", "virus.exe", "application/octet-stream",
                "fake".getBytes()
        );

        var ex = assertThrows(BusinessException.class, () -> novelService.upload(file));
        assertTrue(ex.getMessage().contains("格式"));
    }

    @Test
    void shouldAcceptMarkdownFile() {
        var file = new MockMultipartFile(
                "file", "小说.md", "text/markdown",
                "# 第一章\n\n这是 Markdown 格式的小说内容。".getBytes()
        );

        var result = novelService.upload(file);
        assertEquals("小说.md", result.fileName());
        assertTrue(result.totalChars() > 0);
    }

    @Test
    void shouldGenerateUniqueUuid() {
        var file1 = new MockMultipartFile(
                "file", "a.txt", "text/plain", "内容A".getBytes());
        var file2 = new MockMultipartFile(
                "file", "b.txt", "text/plain", "内容B".getBytes());

        var r1 = novelService.upload(file1);
        var r2 = novelService.upload(file2);

        assertNotEquals(r1.novelId(), r2.novelId());
    }
}
