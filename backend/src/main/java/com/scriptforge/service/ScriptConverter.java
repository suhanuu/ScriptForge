package com.scriptforge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.scriptforge.client.LlmClient;
import com.scriptforge.exception.ConversionException;
import com.scriptforge.model.entity.Chapter;
import com.scriptforge.model.schema.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 剧本转换引擎 —— 核心管道：逐章调 LLM → 解析 YAML → 校验 → 合并 → 润色。
 */
@Slf4j
@Component
public class ScriptConverter {

    private final LlmClient llmClient;
    private final YamlValidator validator;
    private final PromptBuilder promptBuilder;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    private static final Pattern YAML_BLOCK = Pattern.compile(
            "```\\s*(?:yaml|yml)?\\s*\\n(.*?)```", Pattern.DOTALL);

    public ScriptConverter(LlmClient llmClient, YamlValidator validator, PromptBuilder promptBuilder) {
        this.llmClient = llmClient;
        this.validator = validator;
        this.promptBuilder = promptBuilder;
    }

    /** 单章最大字符数，超过则分段转换 */
    private static final int MAX_CHAPTER_LENGTH = 8000;

    /** 第一阶段：逐章转换，超长章节自动分段 */
    public List<ChapterScriptResult> convertChapters(List<Chapter> chapters) {
        List<ChapterScriptResult> results = new ArrayList<>();
        for (Chapter ch : chapters) {
            if (ch.getContent() != null && ch.getContent().length() > MAX_CHAPTER_LENGTH) {
                results.addAll(convertLongChapter(ch));
            } else {
                results.add(convertOneChapter(ch));
            }
        }
        return results;
    }

    /** 超长章节：按场景自然分段，每段附带前一段末尾 2-3 句作为上下文，独立转换后合并 */
    private List<ChapterScriptResult> convertLongChapter(Chapter ch) {
        List<String> segments = splitByNaturalBreaks(ch.getContent());
        List<ScriptOutput> segmentScripts = new ArrayList<>();
        boolean anyFailed = false;
        String prevTail = "";

        for (int i = 0; i < segments.size(); i++) {
            // 前一段末尾 2-3 句作为上下文，帮助 LLM 保持情节连贯
            String contextPrefix = prevTail.isEmpty() ? "" : "(接上文: " + prevTail + ")\n\n";
            String segContent = contextPrefix + segments.get(i);
            // 更新 prevTail 为当前段末尾 2-3 句
            prevTail = extractTail(segments.get(i), 3);

            Chapter seg = Chapter.builder()
                    .chapterNumber(ch.getChapterNumber())
                    .title(ch.getTitle() + "(" + (i + 1) + "/" + segments.size() + ")")
                    .content(segContent)
                    .build();
            var result = convertOneChapter(seg);
            if (result.success()) {
                segmentScripts.add(result.script());
            } else {
                anyFailed = true;
            }
        }

        if (segmentScripts.isEmpty()) {
            return List.of(new ChapterScriptResult(ch.getChapterNumber(), null, "分段转换全部失败"));
        }

        try {
            String merged = merge(segmentScripts, ch.getTitle());
            var parseResult = validator.tryParse(merged);
            return List.of(parseResult.success()
                    ? new ChapterScriptResult(ch.getChapterNumber(), parseResult.script(),
                            anyFailed ? "部分分段转换失败" : null)
                    : new ChapterScriptResult(ch.getChapterNumber(), null, "分段合并后解析失败"));
        } catch (Exception e) {
            return List.of(new ChapterScriptResult(ch.getChapterNumber(), null, "分段合并失败: " + e.getMessage()));
        }
    }

    /** 提取文本末尾最后 n 句话（以句号/问号/感叹号为界） */
    private String extractTail(String text, int sentenceCount) {
        String[] parts = text.split("[。？！]");
        if (parts.length <= sentenceCount) return text.substring(Math.max(0, text.length() - 50));
        StringBuilder tail = new StringBuilder();
        for (int i = parts.length - sentenceCount; i < parts.length; i++) {
            tail.append(parts[i]).append("。");
        }
        return tail.length() > 100 ? tail.substring(tail.length() - 100) : tail.toString();
    }

    /** 按空行或句号+换行自然分段，每段不超过 maxLength */
    private List<String> splitByNaturalBreaks(String content) {
        List<String> segments = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : content.split("\n")) {
            if (current.length() + line.length() > MAX_CHAPTER_LENGTH && !current.isEmpty()) {
                segments.add(current.toString());
                current = new StringBuilder();
            }
            current.append(line).append("\n");
        }
        if (!current.isEmpty()) segments.add(current.toString());
        return segments;
    }

    /** 合并多章 ScriptOutput 为完整剧本 */
    public String merge(List<ScriptOutput> chapterScripts, String novelTitle) throws JsonProcessingException {
        List<CharacterDef> allChars = mergeCharacters(chapterScripts);
        List<LocationDef> allLocs = mergeLocations(chapterScripts);
        List<EpisodeDef> allEpisodes = mergeEpisodes(chapterScripts, allChars, allLocs);

        ScriptOutput merged = ScriptOutput.builder()
                .metadata(ScriptMetadata.builder()
                        .title(novelTitle + " - 剧本")
                        .originalWork(novelTitle)
                        .author("AI转换")
                        .version("1.0")
                        .createdAt(Instant.now().toString())
                        .estimatedDuration(countScenes(allEpisodes) * 2)
                        .build())
                .characters(allChars)
                .locations(allLocs)
                .episodes(allEpisodes)
                .build();

        validator.autoFix(merged);
        return yamlMapper.writeValueAsString(merged);
    }

    /**
     * 第二阶段：对合并后的完整剧本逐场做"去 AI 味"润色。
     * 每场独立发 LLM 请求，只改 dialogues 部分，失败保留原始版本。
     * @return 润色后的完整剧本 YAML
     */
    public String polishAllScenes(String fullYaml) throws JsonProcessingException {
        if (!llmClient.isConfigured()) return fullYaml;

        ScriptOutput script = yamlMapper.readValue(fullYaml, ScriptOutput.class);
        int polished = 0;
        for (var ep : script.getEpisodes()) {
            for (var scene : ep.getScenes()) {
                try {
                    String sceneYaml = yamlMapper.writeValueAsString(scene);
                    String polishedYaml = polishScene(sceneYaml);
                    if (!polishedYaml.equals(sceneYaml)) {
                        SceneDef polishedScene = yamlMapper.readValue(polishedYaml, SceneDef.class);
                        scene.setContent(polishedScene.getContent());
                        polished++;
                    }
                } catch (Exception e) {
                    log.warn("Scene {} 润色失败，保留原始版本: {}", scene.getSceneId(), e.getMessage());
                }
            }
        }
        if (polished > 0) log.info("已润色 {} 场", polished);
        return yamlMapper.writeValueAsString(script);
    }

    /** 对单个 scene 做润色（只改对话），失败不阻塞 */
    private String polishScene(String sceneYaml) {
        if (!llmClient.isConfigured()) {
            return sceneYaml;
        }
        try {
            String polishPrompt = """
你是一位专业对白编剧。请润色以下剧本场景的对话，使其更口语化。

规则：1) 保留原意和剧情信息 2) 用口语词汇和短句 3) 去掉书面语表达（'鉴于''因此''然而'→口语替代）
4) 如果原文已足够口语化则原样保留

只返回润色后的完整 scene YAML，用 ```yaml 包裹。

""" + sceneYaml;

            String result = llmClient.chat(polishPrompt, polishPrompt);
            return extractYaml(result);
        } catch (Exception e) {
            log.warn("润色失败，保留原始版本: {}", e.getMessage());
            return sceneYaml;
        }
    }

    // ---- 内部方法 ----

    private ChapterScriptResult convertOneChapter(Chapter ch) {
        String userPrompt = promptBuilder.buildUserPrompt(ch.getChapterNumber(), ch.getTitle(), ch.getContent());
        String systemPrompt = promptBuilder.buildSystemPrompt();

        for (int attempt = 0; attempt <= 2; attempt++) {
            try {
                String raw = llmClient.chat(systemPrompt, userPrompt);
                String yaml = extractYaml(raw);
                var parseResult = validator.tryParse(yaml);
                if (!parseResult.success()) {
                    log.warn("第{}章 第{}次尝试解析失败: {}", ch.getChapterNumber(), attempt + 1, parseResult.error());
                    if (attempt < 2) {
                        userPrompt = userPrompt + "\n\n上一次输出不符合 YAML Schema，请严格按照指定格式重新输出，只输出 ```yaml 代码块";
                    }
                    continue;
                }

                var errors = validator.validate(parseResult.script());
                if (errors.isEmpty()) {
                    return new ChapterScriptResult(ch.getChapterNumber(), parseResult.script(), null);
                }

                log.warn("第{}章 第{}次尝试校验失败: {}", ch.getChapterNumber(), attempt + 1, errors);
                validator.autoFix(parseResult.script());
                var remaining = validator.validate(parseResult.script());
                if (remaining.isEmpty()) {
                    return new ChapterScriptResult(ch.getChapterNumber(), parseResult.script(), null);
                }
                if (attempt < 2) {
                    userPrompt = userPrompt + "\n\n上一次输出校验未通过: " + String.join("; ", remaining) + "。请修正后重新输出。";
                }
            } catch (Exception e) {
                log.error("第{}章 第{}次尝试 LLM 调用失败: {}", ch.getChapterNumber(), attempt + 1, e.getMessage());
            }
        }
        return new ChapterScriptResult(ch.getChapterNumber(), null, "转换失败，已重试2次");
    }

    /** 从 LLM 输出中提取 YAML 代码块 */
    String extractYaml(String llmOutput) {
        Matcher m = YAML_BLOCK.matcher(llmOutput);
        if (m.find()) return m.group(1).trim();
        // 尝试直接作为 YAML 解析
        return llmOutput.trim();
    }

    private List<CharacterDef> mergeCharacters(List<ScriptOutput> scripts) {
        Map<String, CharacterDef> seen = new LinkedHashMap<>();
        for (var s : scripts) {
            if (s.getCharacters() != null) {
                for (var c : s.getCharacters()) {
                    seen.putIfAbsent(c.getName(), c);
                }
            }
        }
        return new ArrayList<>(seen.values());
    }

    private List<LocationDef> mergeLocations(List<ScriptOutput> scripts) {
        Map<String, LocationDef> seen = new LinkedHashMap<>();
        for (var s : scripts) {
            if (s.getLocations() != null) {
                for (var l : s.getLocations()) {
                    seen.putIfAbsent(l.getName(), l);
                }
            }
        }
        return new ArrayList<>(seen.values());
    }

    private List<EpisodeDef> mergeEpisodes(List<ScriptOutput> scripts, List<CharacterDef> chars, List<LocationDef> locs) {
        Set<String> charIds = chars.stream().map(CharacterDef::getId).collect(Collectors.toSet());
        List<EpisodeDef> all = new ArrayList<>();
        int episodeId = 1;
        for (var s : scripts) {
            if (s.getEpisodes() != null) {
                for (var ep : s.getEpisodes()) {
                    for (var scene : ep.getScenes()) {
                        // 过滤不存在的角色引用
                        if (scene.getCharactersPresent() != null) {
                            scene.setCharactersPresent(
                                    scene.getCharactersPresent().stream()
                                            .filter(charIds::contains)
                                            .collect(Collectors.toList())
                            );
                        }
                    }
                    ep.setEpisodeId(episodeId++);
                    all.add(ep);
                }
            }
        }
        return all;
    }

    private int countScenes(List<EpisodeDef> episodes) {
        return episodes.stream().mapToInt(e -> e.getScenes().size()).sum();
    }

    /** 单章转换结果 */
    public record ChapterScriptResult(int chapterNumber, ScriptOutput script, String error) {
        public boolean success() { return script != null; }
    }
}
