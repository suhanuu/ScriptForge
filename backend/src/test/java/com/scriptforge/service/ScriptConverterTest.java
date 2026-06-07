package com.scriptforge.service;

import com.scriptforge.client.LlmClient;
import com.scriptforge.model.entity.Chapter;
import com.scriptforge.model.schema.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScriptConverterTest {

    @Mock
    private LlmClient llmClient;

    private ScriptConverter converter;
    private final YamlValidator validator = new YamlValidator();
    private final PromptBuilder promptBuilder = new PromptBuilder();

    @BeforeEach
    void setUp() {
        lenient().when(llmClient.isConfigured()).thenReturn(true);
        converter = new ScriptConverter(llmClient, validator, promptBuilder);
    }

    @Test
    void shouldExtractYamlFromCodeBlock() {
        String raw = "前面一些废话\n```yaml\nmetadata:\n  title: \"测试\"\n  original_work: \"原作\"\n  author: \"作者\"\ncharacters: []\nlocations: []\nepisodes: []\n```\n后面更多废话";

        String yaml = converter.extractYaml(raw);
        assertTrue(yaml.contains("测试"));
        assertFalse(yaml.contains("```"));
    }

    @Test
    void shouldExtractPlainYamlWhenNoCodeBlock() {
        String raw = "metadata:\n  title: \"直接YAML\"\n  original_work: \"原作\"\n  author: \"作者\"\ncharacters: []\nlocations: []\nepisodes: []";

        String yaml = converter.extractYaml(raw);
        assertTrue(yaml.contains("直接YAML"));
    }

    @Test
    void shouldMergeMultipleChapterScripts() throws Exception {
        // 构造两章 LLM 返回的模拟 YAML
        String ch1Yaml = """
```yaml
metadata:
  title: "第1章"
  original_work: "测试小说"
  author: "AI"
characters:
  - id: "zhang_san"
    name: "张三"
    role: "主角"
    description: ""
locations:
  - id: "home"
    name: "张三的家"
    type: "内景"
    description: ""
episodes:
  - episode_id: 1
    title: "第一集"
    scenes:
      - scene_id: "1-1"
        location: "home"
        time: "白天"
        weather: "晴"
        characters_present: ["zhang_san"]
        content:
          - type: "action"
            text: "张三醒来"
          - type: "transition"
            effect: "无"
            next_scene: null
```
""";

        when(llmClient.chat(anyString(), anyString())).thenReturn(ch1Yaml);

        Chapter ch1 = Chapter.builder().chapterNumber(1).title("第一章").content("张三醒来。").build();
        var results = converter.convertChapters(List.of(ch1));
        assertEquals(1, results.size());
        assertTrue(results.get(0).success());

        // Merge single chapter
        String merged = converter.merge(List.of(results.get(0).script()), "测试小说");
        assertTrue(merged.contains("测试小说"));
        assertTrue(merged.contains("张三"));
    }

    @Test
    void shouldRetryOnParseFailure() {
        // 前两次返回无效内容，第三次返回有效 YAML
        String validYaml = """
```yaml
metadata:
  title: "T"
  original_work: "O"
  author: "A"
characters: []
locations: []
episodes:
  - episode_id: 1
    title: "E1"
    scenes:
      - scene_id: "1-1"
        location: "L"
        content:
          - type: "action"
            text: "X"
          - type: "transition"
            effect: "无"
            next_scene: null
```
""";

        when(llmClient.chat(anyString(), anyString()))
                .thenReturn("not valid yaml at all {{{")
                .thenReturn("still not valid [[[ yaml")
                .thenReturn(validYaml);

        Chapter ch = Chapter.builder().chapterNumber(1).title("第一章").content("测试内容。").build();
        var results = converter.convertChapters(List.of(ch));
        assertEquals(1, results.size());
        assertTrue(results.get(0).success(), "Should succeed after retries, got error: " + results.get(0).error());
    }

    @Test
    void shouldSplitLongChapter() {
        // 构造超过 8000 字的内容
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            longContent.append("重复内容用于测试超长章节分段处理机制。\n");
        }
        String yaml = """
```yaml
metadata:
  title: "T"
  original_work: "O"
  author: "A"
characters: []
locations: []
episodes:
  - episode_id: 1
    title: "E1"
    scenes:
      - scene_id: "1-1"
        location: "L"
        content:
          - type: "action"
            text: "X"
          - type: "transition"
            effect: "无"
            next_scene: null
```
""";
        when(llmClient.chat(anyString(), anyString())).thenReturn(yaml);
        Chapter ch = Chapter.builder().chapterNumber(1).title("超长章").content(longContent.toString()).build();
        var results = converter.convertChapters(List.of(ch));
        assertEquals(1, results.size());
        assertTrue(results.get(0).success(), "Long chapter should convert with segmentation");
    }

    @Test
    void shouldReturnErrorAfterAllRetries() {
        when(llmClient.chat(anyString(), anyString())).thenReturn("invalid yaml {{{");

        Chapter ch = Chapter.builder().chapterNumber(1).title("第一章").content("测试。").build();
        var results = converter.convertChapters(List.of(ch));
        assertEquals(1, results.size());
        assertFalse(results.get(0).success());
        assertNotNull(results.get(0).error());
    }

    @Test
    void shouldSkipPolishWhenNotConfigured() {
        when(llmClient.isConfigured()).thenReturn(false);

        String sceneYaml = "metadata:\n  title: \"T\"\noriginal_work: \"O\"\nauthor: \"A\"\n";
        String result = converter.polishScene(sceneYaml);
        assertEquals(sceneYaml, result, "Should return original when LLM not configured");
    }
}
