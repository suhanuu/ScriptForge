package com.scriptforge.controller;

import com.scriptforge.client.LlmClient;
import com.scriptforge.exception.BusinessException;
import com.scriptforge.model.dto.ChapterDto;
import com.scriptforge.model.dto.UploadResultDto;
import com.scriptforge.service.NovelService;
import com.scriptforge.service.ScriptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NovelController.class)
class NovelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NovelService novelService;

    @MockBean
    private ScriptService scriptService;

    @MockBean
    private LlmClient llmClient;

    @Test
    void shouldUploadTxtFileAndReturnCorrectJson() throws Exception {
        when(novelService.upload(any()))
                .thenReturn(new UploadResultDto("abc-123", "故事.txt", 500,
                        List.of(new ChapterDto(1, "第一章 开始", "这是测试内容。", 7))));

        var file = new MockMultipartFile(
                "file", "故事.txt", "text/plain",
                "第一章 开始\n这是测试内容。".getBytes()
        );

        mockMvc.perform(multipart("/api/novels/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.novelId").value("abc-123"))
                .andExpect(jsonPath("$.data.fileName").value("故事.txt"))
                .andExpect(jsonPath("$.data.totalChars").value(500))
                .andExpect(jsonPath("$.data.chapters.length()").value(1))
                .andExpect(jsonPath("$.data.chapters[0].title").value("第一章 开始"));
    }

    @Test
    void shouldReturn400WhenServiceThrowsBusinessException() throws Exception {
        when(novelService.upload(any()))
                .thenThrow(new BusinessException("文件内容为空"));

        var file = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]
        );

        mockMvc.perform(multipart("/api/novels/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("文件内容为空"));
    }

    @Test
    void shouldReturn400ForUnsupportedFileFormat() throws Exception {
        when(novelService.upload(any()))
                .thenThrow(new BusinessException("仅支持 TXT/MD 格式文件"));

        var file = new MockMultipartFile(
                "file", "test.exe", "application/octet-stream",
                "malware".getBytes()
        );

        mockMvc.perform(multipart("/api/novels/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
