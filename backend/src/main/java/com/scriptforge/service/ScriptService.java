package com.scriptforge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scriptforge.client.LlmClient;
import com.scriptforge.exception.BusinessException;
import com.scriptforge.mapper.ChapterMapper;
import com.scriptforge.mapper.NovelMapper;
import com.scriptforge.mapper.ScriptMapper;
import com.scriptforge.model.dto.ConvertRequestDto;
import com.scriptforge.model.entity.Chapter;
import com.scriptforge.model.entity.Novel;
import com.scriptforge.model.entity.Script;
import com.scriptforge.model.schema.ScriptOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** 剧本转换编排服务 —— 协调 NovelMapper/ChapterMapper/ScriptMapper + ScriptConverter */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScriptService {

    private final NovelMapper novelMapper;
    private final ChapterMapper chapterMapper;
    private final ScriptMapper scriptMapper;
    private final ScriptConverter scriptConverter;
    private final LlmClient llmClient;

    @Transactional
    public ConvertResult convert(ConvertRequestDto request) {
        // LLM 未配置时优雅降级
        if (!llmClient.isConfigured()) {
            throw new BusinessException(503, "LLM 服务未配置，请在 .env 中设置 LLM_API_KEY");
        }

        // 根据 uuid 查 novel
        Novel novel = novelMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Novel>()
                        .eq(Novel::getUuid, request.getNovelUuid())
        );
        if (novel == null) {
            throw new BusinessException(404, "小说不存在");
        }

        // 获取章节
        List<Chapter> chapters;
        if (request.getChapterNumbers() != null && !request.getChapterNumbers().isEmpty()) {
            chapters = chapterMapper.findByNovelIdAndChapterNumbers(novel.getId(), request.getChapterNumbers());
        } else {
            chapters = chapterMapper.findByNovelIdOrderByChapterNumberAsc(novel.getId());
        }
        if (chapters.isEmpty()) {
            throw new BusinessException("没有可转换的章节");
        }

        // 删除旧剧本
        Script oldScript = scriptMapper.findByNovelId(novel.getId());
        if (oldScript != null) {
            scriptMapper.deleteById(oldScript.getId());
        }

        // 创建 Script 记录
        Script script = Script.builder().novelId(novel.getId()).status("CONVERTING").build();
        scriptMapper.insert(script);

        // 逐章转换
        List<ConvertResult.ChapterProgress> progressList = new ArrayList<>();
        List<ScriptOutput> chapterScripts = new ArrayList<>();

        for (Chapter ch : chapters) {
            var cp = ConvertResult.ChapterProgress.builder()
                    .chapterNumber(ch.getChapterNumber()).chapterTitle(ch.getTitle()).status("CONVERTING").build();
            progressList.add(cp);

            try {
                var result = scriptConverter.convertChapters(List.of(ch));
                if (!result.isEmpty() && result.get(0).success()) {
                    chapterScripts.add(result.get(0).script());
                    cp.setStatus("DONE");
                } else {
                    cp.setStatus("ERROR");
                    cp.setErrorMessage(result.isEmpty() ? "未知错误" : result.get(0).error());
                }
            } catch (Exception e) {
                cp.setStatus("ERROR");
                cp.setErrorMessage(e.getMessage());
            }
        }

        // 合并
        long doneCount = progressList.stream().filter(p -> "DONE".equals(p.getStatus())).count();
        if (doneCount == 0) {
            script.setStatus("ERROR");
            script.setErrorMessage("所有章节转换失败");
            scriptMapper.updateById(script);
            return new ConvertResult(script.getId(), script.getStatus(), null, progressList);
        }

        try {
            String yaml = scriptConverter.merge(chapterScripts, novel.getFileName());
            script.setYamlContent(yaml);
            script.setScenesCount(countScenes(chapterScripts));
            script.setStatus(doneCount == chapters.size() ? "READY" : "PARTIAL_ERROR");
            scriptMapper.updateById(script);

            return new ConvertResult(script.getId(), script.getStatus(), yaml, progressList);
        } catch (JsonProcessingException e) {
            script.setStatus("ERROR");
            script.setErrorMessage("YAML 合并失败: " + e.getMessage());
            scriptMapper.updateById(script);
            return new ConvertResult(script.getId(), "ERROR", null, progressList);
        }
    }

    /** 获取剧本结果 */
    public String getYaml(Long scriptId) {
        Script script = scriptMapper.selectById(scriptId);
        if (script == null) throw new BusinessException(404, "剧本不存在");
        if (script.getYamlContent() == null) throw new BusinessException("剧本 YAML 尚未生成");
        return script.getYamlContent();
    }

    /** 获取剧本信息 */
    public Script getScript(Long scriptId) {
        Script script = scriptMapper.selectById(scriptId);
        if (script == null) throw new BusinessException(404, "剧本不存在");
        return script;
    }

    /** 保存编辑后的 YAML 到数据库 */
    public void saveYaml(Long scriptId, String yamlContent) {
        Script script = scriptMapper.selectById(scriptId);
        if (script == null) throw new BusinessException(404, "剧本不存在");
        script.setYamlContent(yamlContent);
        scriptMapper.updateById(script);
    }

    private int countScenes(List<ScriptOutput> scripts) {
        return scripts.stream()
                .flatMap(s -> s.getEpisodes().stream())
                .mapToInt(e -> e.getScenes().size())
                .sum();
    }

    /** 转换结果 */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ConvertResult {
        private Long scriptId;
        private String status;
        private String yamlContent;
        private List<ChapterProgress> chapterProgress;

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class ChapterProgress {
            private int chapterNumber;
            private String chapterTitle;
            private String status;
            private String errorMessage;
        }
    }
}
