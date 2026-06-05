package com.scriptforge.controller;

import com.scriptforge.model.dto.SfResult;
import com.scriptforge.model.dto.UploadResultDto;
import com.scriptforge.service.NovelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/novels")
@RequiredArgsConstructor
public class NovelController {

    private final NovelService novelService;

    @PostMapping("/upload")
    public SfResult<UploadResultDto> upload(@RequestParam("file") MultipartFile file) {
        return SfResult.success(novelService.upload(file));
    }
}
