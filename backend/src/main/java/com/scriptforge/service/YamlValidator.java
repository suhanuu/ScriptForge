package com.scriptforge.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.scriptforge.model.schema.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * YAML Schema 校验器 —— 验证 LLM 输出的剧本是否符合 Schema 定义，
 * 并尝试自动修复常见格式问题。
 */
@Slf4j
@Component
public class YamlValidator {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /** 校验 ScriptOutput，返回错误列表，空列表表示通过 */
    public List<String> validate(ScriptOutput script) {
        List<String> errors = new ArrayList<>();

        // metadata
        if (script.getMetadata() == null) {
            errors.add("缺少 metadata");
        } else {
            var m = script.getMetadata();
            if (isBlank(m.getTitle())) errors.add("metadata.title 为空");
            if (isBlank(m.getOriginalWork())) errors.add("metadata.original_work 为空");
            if (isBlank(m.getAuthor())) errors.add("metadata.author 为空");
        }

        // characters
        if (script.getCharacters() != null) {
            Set<String> ids = new HashSet<>();
            for (var c : script.getCharacters()) {
                if (isBlank(c.getId())) errors.add("角色缺少 id");
                else if (!ids.add(c.getId())) errors.add("重复的角色 id: " + c.getId());
                if (isBlank(c.getName())) errors.add("角色 " + c.getId() + " 缺少 name");
            }
        }

        // locations
        if (script.getLocations() != null) {
            Set<String> locIds = new HashSet<>();
            for (var l : script.getLocations()) {
                if (isBlank(l.getId())) errors.add("地点缺少 id");
                else if (!locIds.add(l.getId())) errors.add("重复的地点 id: " + l.getId());
            }
        }

        // episodes
        if (script.getEpisodes() == null || script.getEpisodes().isEmpty()) {
            errors.add("episodes 为空");
        } else {
            Set<String> charIds = script.getCharacters() != null
                    ? script.getCharacters().stream().map(CharacterDef::getId).collect(Collectors.toSet())
                    : Set.of();
            Set<String> locIds = script.getLocations() != null
                    ? script.getLocations().stream().map(LocationDef::getId).collect(Collectors.toSet())
                    : Set.of();

            for (var ep : script.getEpisodes()) {
                for (var scene : ep.getScenes()) {
                    if (isBlank(scene.getSceneId())) errors.add("scene 缺少 scene_id");
                    if (isBlank(scene.getLocation())) errors.add("scene " + scene.getSceneId() + " 缺少 location");
                    if (scene.getContent() == null || scene.getContent().isEmpty()) {
                        errors.add("scene " + scene.getSceneId() + " content 为空");
                        continue;
                    }
                    for (int i = 0; i < scene.getContent().size(); i++) {
                        var elem = scene.getContent().get(i);
                        if (elem instanceof DialogueElement d) {
                            if (isBlank(d.getCharacter())) errors.add("scene " + scene.getSceneId() + " dialogue[" + i + "] 缺少 character");
                            if (isBlank(d.getText())) errors.add("scene " + scene.getSceneId() + " dialogue[" + i + "] 缺少 text");
                        }
                        if (elem instanceof TransitionElement t) {
                            if (isBlank(t.getEffect())) errors.add("scene " + scene.getSceneId() + " transition 缺少 effect");
                        }
                    }
                }
            }
        }

        return errors;
    }

    /** 尝试解析 YAML 字符串，错误信息只保留字段名和位置，不输出完整异常栈 */
    public ParseResult tryParse(String yaml) {
        try {
            ScriptOutput script = yamlMapper.readValue(yaml, ScriptOutput.class);
            return new ParseResult(script, null);
        } catch (Exception e) {
            String msg = e.getMessage();
            // 精简：只保留 "Unrecognized field" 或具体的解析错误位置
            int atIdx = msg.indexOf(" at [Source:");
            if (atIdx > 0) msg = msg.substring(0, atIdx);
            return new ParseResult(null, msg);
        }
    }

    /** 自动修复常见问题 */
    public ScriptOutput autoFix(ScriptOutput script) {
        if (script.getMetadata() == null) {
            script.setMetadata(ScriptMetadata.builder().title("未命名").originalWork("未知").author("AI转换").build());
        }
        if (script.getCharacters() == null) script.setCharacters(List.of());
        if (script.getLocations() == null) script.setLocations(List.of());
        if (script.getEpisodes() == null) script.setEpisodes(List.of());

        int sceneCount = 0;
        for (var ep : script.getEpisodes()) {
            for (var scene : ep.getScenes()) {
                sceneCount++;
                if (scene.getContent() == null) scene.setContent(List.of());
                if (scene.getCharactersPresent() == null) scene.setCharactersPresent(List.of());
                // 修复空 title/effect
                if (scene.getTime() == null) scene.setTime("");
                if (scene.getWeather() == null) scene.setWeather("");
                for (var elem : scene.getContent()) {
                    if (elem instanceof TransitionElement t && t.getEffect() == null) {
                        t.setEffect("无");
                    }
                }
            }
        }

        script.getMetadata().setEstimatedDuration(Math.max(1, sceneCount * 2));
        log.info("autoFix applied, {} scenes found", sceneCount);
        return script;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** 解析结果 */
    public record ParseResult(ScriptOutput script, String error) {
        public boolean success() { return script != null; }
    }
}
