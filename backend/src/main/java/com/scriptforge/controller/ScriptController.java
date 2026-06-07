package com.scriptforge.controller;

import com.scriptforge.model.dto.ConvertRequestDto;
import com.scriptforge.model.dto.SfResult;
import com.scriptforge.service.ScriptService;
import com.scriptforge.service.YamlValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/** 剧本转换与下载控制器 */
@RestController
@RequestMapping("/api/scripts")
@RequiredArgsConstructor
public class ScriptController {

    private final ScriptService scriptService;
    private final YamlValidator yamlValidator;

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

    /** Schema 校验 YAML */
    @PostMapping("/validate")
    public SfResult<List<String>> validate(@RequestBody Map<String, String> body) {
        String yaml = body.get("yaml");
        if (yaml == null || yaml.isBlank()) return SfResult.error(400, "YAML 内容为空");
        var result = yamlValidator.tryParse(yaml);
        if (!result.success()) return SfResult.error(400, "YAML 解析失败: " + result.error());
        return SfResult.success(yamlValidator.validate(result.script()));
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
