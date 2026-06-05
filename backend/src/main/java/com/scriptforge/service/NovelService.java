package com.scriptforge.service;

import com.scriptforge.exception.BusinessException;
import com.scriptforge.mapper.NovelMapper;
import com.scriptforge.model.dto.UploadResultDto;
import com.scriptforge.model.entity.Novel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NovelService {

    private final NovelMapper novelMapper;

    public UploadResultDto upload(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new BusinessException("文件名不能为空");
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
        log.info("Uploaded novel {} (uuid={}), {} chars", originalName, uuid, totalChars);

        return new UploadResultDto(uuid, originalName, totalChars);
    }
}
