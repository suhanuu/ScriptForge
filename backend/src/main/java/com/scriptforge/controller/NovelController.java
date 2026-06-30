package com.scriptforge.controller;

import com.scriptforge.model.dto.SfResult;
import com.scriptforge.model.dto.UploadResultDto;
import com.scriptforge.model.entity.Chapter;
import com.scriptforge.model.entity.Novel;
import com.scriptforge.service.NovelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/novels")
@RequiredArgsConstructor
@Tag(name = "小说管理")
public class NovelController {

    private final NovelService novelService;

    @PostMapping("/upload")
    @Operation(summary = "上传小说")
    public SfResult<UploadResultDto> upload(@RequestParam("file") MultipartFile file) {
        return SfResult.success(novelService.upload(file));
    }

    /** 按 UUID 查询小说原文（用于前端展示） */
    @Operation(summary = "按 UUID 查询小说原文")
    @GetMapping("/by-uuid/{uuid}")
    public SfResult<Novel> getByUuid(@PathVariable String uuid) {
        return SfResult.success(novelService.getByUuid(uuid));
    }

    /** 按 UUID 查询小说的章节列表 */
    @GetMapping("/by-uuid/{uuid}/chapters")
    @Operation(summary = "按 UUID 获取小说的章节列表")
    public SfResult<List<Chapter>> getChaptersByUuid(@PathVariable String uuid) {
        return SfResult.success(novelService.getChaptersByUuid(uuid));
    }
}
