package com.scriptforge.controller;

import com.scriptforge.model.dto.ConvertRequestDto;
import com.scriptforge.model.dto.SfResult;
import com.scriptforge.service.ScriptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/** 剧本转换与下载控制器 */
@RestController
@RequestMapping("/api/scripts")
@RequiredArgsConstructor
public class ScriptController {

    private final ScriptService scriptService;

    /** 发起转换 */
    @PostMapping("/convert")
    public SfResult<ScriptService.ConvertResult> convert(@Valid @RequestBody ConvertRequestDto request) {
        return SfResult.success(scriptService.convert(request));
    }

    /** 获取剧本详情 */
    @GetMapping("/{id}")
    public SfResult<ScriptService.ConvertResult> getResult(@PathVariable Long id) {
        var script = scriptService.getScript(id);
        return SfResult.success(ScriptService.ConvertResult.builder()
                .scriptId(script.getId()).status(script.getStatus())
                .yamlContent(script.getYamlContent()).build());
    }

    /** 保存编辑后的 YAML */
    @PutMapping("/{id}/yaml")
    public SfResult<Void> saveYaml(@PathVariable Long id, @RequestBody Map<String, String> body) {
        scriptService.saveYaml(id, body.get("yamlContent"));
        return SfResult.success();
    }

    /** 下载 YAML 文件 */
    @GetMapping("/{id}/yaml")
    public ResponseEntity<byte[]> downloadYaml(@PathVariable Long id) {
        String yaml = scriptService.getYaml(id);
        byte[] bytes = yaml.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"script.yaml\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
