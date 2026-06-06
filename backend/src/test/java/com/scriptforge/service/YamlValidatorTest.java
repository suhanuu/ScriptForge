package com.scriptforge.service;

import com.scriptforge.model.schema.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class YamlValidatorTest {

    private final YamlValidator validator = new YamlValidator();

    @Test
    void shouldPassValidScript() {
        ScriptOutput script = ScriptOutput.builder()
                .metadata(ScriptMetadata.builder().title("测试").originalWork("原作").author("作者").build())
                .characters(List.of(CharacterDef.builder().id("c1").name("张三").role("主角").build()))
                .locations(List.of(LocationDef.builder().id("l1").name("办公室").type("内景").build()))
                .episodes(List.of(EpisodeDef.builder().episodeId(1).title("第一集")
                        .scenes(List.of(SceneDef.builder().sceneId("1-1").location("l1")
                                .content(List.of(
                                        ActionElement.builder().text("动作").build(),
                                        TransitionElement.builder().effect("切入").nextScene(null).build()
                                )).build()))
                        .build()))
                .build();

        List<String> errors = validator.validate(script);
        assertTrue(errors.isEmpty(), "Valid script should have no errors: " + errors);
    }

    @Test
    void shouldDetectMissingTitle() {
        ScriptOutput script = ScriptOutput.builder()
                .metadata(ScriptMetadata.builder().originalWork("原作").author("作者").build())
                .characters(List.of())
                .locations(List.of())
                .episodes(List.of())
                .build();

        List<String> errors = validator.validate(script);
        assertTrue(errors.stream().anyMatch(e -> e.contains("title")), "Should detect missing title");
    }

    @Test
    void shouldDetectDuplicateCharacterId() {
        ScriptOutput script = ScriptOutput.builder()
                .metadata(ScriptMetadata.builder().title("T").originalWork("O").author("A").build())
                .characters(List.of(
                        CharacterDef.builder().id("dup").name("张三").role("主角").build(),
                        CharacterDef.builder().id("dup").name("李四").role("配角").build()
                ))
                .locations(List.of())
                .episodes(List.of())
                .build();

        List<String> errors = validator.validate(script);
        assertTrue(errors.stream().anyMatch(e -> e.contains("重复")), "Should detect duplicate id");
    }

    @Test
    void shouldDetectDialogueMissingCharacter() {
        ScriptOutput script = ScriptOutput.builder()
                .metadata(ScriptMetadata.builder().title("T").originalWork("O").author("A").build())
                .characters(List.of())
                .locations(List.of())
                .episodes(List.of(EpisodeDef.builder().episodeId(1).title("E1")
                        .scenes(List.of(SceneDef.builder().sceneId("1-1").location("l1")
                                .content(List.of(DialogueElement.builder().text("台词").build()))
                                .build()))
                        .build()))
                .build();

        List<String> errors = validator.validate(script);
        assertTrue(errors.stream().anyMatch(e -> e.contains("character")), "Should detect missing character in dialogue");
    }

    @Test
    void shouldAutoFixMissingFields() {
        ScriptOutput script = ScriptOutput.builder()
                .metadata(ScriptMetadata.builder().title("T").build())
                .episodes(List.of(EpisodeDef.builder().episodeId(1).title("E1")
                        .scenes(List.of(SceneDef.builder().sceneId("1-1").location("l1")
                                .content(List.of(ActionElement.builder().text("动作").build()))
                                .build()))
                        .build()))
                .build();

        ScriptOutput fixed = validator.autoFix(script);
        assertNotNull(fixed.getCharacters());
        assertNotNull(fixed.getLocations());
        assertEquals("T", fixed.getMetadata().getTitle());
    }

    @Test
    void shouldParseValidYaml() {
        String yaml = """
                metadata:
                  title: "测试"
                  original_work: "原作"
                  author: "作者"
                characters: []
                locations: []
                episodes:
                  - episode_id: 1
                    title: "第一集"
                    scenes:
                      - scene_id: "1-1"
                        location: "loc"
                        content:
                          - type: "action"
                            text: "动作"
                          - type: "transition"
                            effect: "无"
                            next_scene: null
                """;

        var result = validator.tryParse(yaml);
        assertTrue(result.success());
        assertEquals("测试", result.script().getMetadata().getTitle());
    }

    @Test
    void shouldFailOnInvalidYaml() {
        var result = validator.tryParse("this is not valid: [[[ yaml!!!");
        assertFalse(result.success());
        assertNotNull(result.error());
    }
}
