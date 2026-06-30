package com.scriptforge.controller;

import com.scriptforge.model.dto.SfResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@Tag(name = "健康检查")
public class HealthController {

    @GetMapping("/api/health")
    @Tag(name = "健康检查", description = "检查服务是否正常")
    public SfResult<Map<String, String>> health() {
        return SfResult.success(Map.of(
                "status", "ok",
                "timestamp", Instant.now().toString()
        ));
    }
}
