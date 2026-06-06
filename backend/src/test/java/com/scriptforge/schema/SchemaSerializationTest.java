package com.scriptforge.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.scriptforge.model.schema.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SchemaSerializationTest {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @Test
    void shouldSerializeAndDeserializeFullScript() throws Exception {
        ScriptOutput script = buildSampleScript();
        String yaml = yamlMapper.writeValueAsString(script);
        assertNotNull(yaml);
        assertTrue(yaml.contains("测试剧本"));

        ScriptOutput parsed = yamlMapper.readValue(yaml, ScriptOutput.class);
        assertEquals("测试剧本", parsed.getMetadata().getTitle());
        assertEquals("li_wei", parsed.getCharacters().get(0).getId());

        SceneDef scene = parsed.getEpisodes().get(0).getScenes().get(0);
        assertEquals(3, scene.getContent().size());
        assertInstanceOf(ActionElement.class, scene.getContent().get(0));
        assertInstanceOf(DialogueElement.class, scene.getContent().get(1));
        assertInstanceOf(TransitionElement.class, scene.getContent().get(2));
    }

    /** 验证能解析 LLM 输出格式的 YAML（type: action 而非 !<action> 标签） */
    @Test
    void shouldParseLlmStyleYaml() throws Exception {
        String llmYaml = """
                metadata:
                  title: "深夜来客"
                  original_work: "深夜来客"
                  author: "未知"
                  version: "1.0"
                  created_at: "2026-06-06T00:00:00Z"
                  estimated_duration: 5
                characters:
                  - id: "lin_yuan"
                    name: "林远"
                    role: "主角"
                    description: "28岁程序员"
                locations:
                  - id: "apt"
                    name: "公寓"
                    type: "内景"
                    description: "普通公寓"
                episodes:
                  - episode_id: 1
                    title: "第一集"
                    scenes:
                      - scene_id: "1-1"
                        location: "apt"
                        time: "深夜"
                        weather: "雨"
                        characters_present: ["lin_yuan"]
                        content:
                          - type: "action"
                            text: "手机闹钟响起"
                          - type: "dialogue"
                            character: "lin_yuan"
                            text: "关掉。"
                            parenthetical: "哑着嗓子"
                          - type: "transition"
                            effect: "切入"
                            next_scene: "1-2"
                      - scene_id: "1-2"
                        location: "apt"
                        time: "凌晨"
                        weather: "雨"
                        characters_present: ["lin_yuan"]
                        content:
                          - type: "action"
                            text: "林远从床上爬起来"
                          - type: "transition"
                            effect: "无"
                            next_scene: null
                """;

        ScriptOutput parsed = yamlMapper.readValue(llmYaml, ScriptOutput.class);

        assertEquals("深夜来客", parsed.getMetadata().getTitle());
        assertEquals(1, parsed.getCharacters().size());
        assertEquals("lin_yuan", parsed.getCharacters().get(0).getId());
        assertEquals(2, parsed.getEpisodes().get(0).getScenes().size());

        SceneDef scene1 = parsed.getEpisodes().get(0).getScenes().get(0);
        assertInstanceOf(ActionElement.class, scene1.getContent().get(0));
        assertInstanceOf(DialogueElement.class, scene1.getContent().get(1));
        assertInstanceOf(TransitionElement.class, scene1.getContent().get(2));

        DialogueElement d = (DialogueElement) scene1.getContent().get(1);
        assertEquals("关掉。", d.getText());
        assertEquals("哑着嗓子", d.getParenthetical());

        TransitionElement t = (TransitionElement) scene1.getContent().get(2);
        assertEquals("切入", t.getEffect());
        assertEquals("1-2", t.getNextScene());
    }

    @Test
    void shouldHandleMinimalScript() throws Exception {
        ScriptOutput script = ScriptOutput.builder()
                .metadata(ScriptMetadata.builder().title("最小").originalWork("原").author("佚名").build())
                .characters(List.of())
                .locations(List.of())
                .episodes(List.of())
                .build();

        String yaml = yamlMapper.writeValueAsString(script);
        ScriptOutput parsed = yamlMapper.readValue(yaml, ScriptOutput.class);
        assertEquals("最小", parsed.getMetadata().getTitle());
    }

    private ScriptOutput buildSampleScript() {
        return ScriptOutput.builder()
                .metadata(ScriptMetadata.builder()
                        .title("测试剧本").originalWork("测试原作").author("未知")
                        .version("1.0").createdAt("2026-06-06T00:00:00Z").estimatedDuration(5)
                        .build())
                .characters(List.of(CharacterDef.builder()
                        .id("li_wei").name("李薇").role("主角").description("项目组长").build()))
                .locations(List.of(LocationDef.builder()
                        .id("office").name("办公室").type("内景").description("开放式办公区").build()))
                .episodes(List.of(EpisodeDef.builder().episodeId(1).title("第一集")
                        .scenes(List.of(SceneDef.builder().sceneId("1-1").location("office")
                                .time("白天").weather("晴").charactersPresent(List.of("li_wei"))
                                .content(List.of(
                                        ActionElement.builder().text("李薇推开门").build(),
                                        DialogueElement.builder()
                                                .character("li_wei").text("开始吧。")
                                                .parenthetical("坚定地").build(),
                                        TransitionElement.builder()
                                                .effect("无").nextScene(null).build()))
                                .build()))
                        .build()))
                .build();
    }
}
